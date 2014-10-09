(ns mixradio.instrumented-jetty
  "Adapter for the Jetty webserver."
  (:require [environ.core :refer [env]]
            [metrics.core :refer [default-registry]]
            [ring.util.servlet :as servlet])
  (:import [ch.qos.logback.access.jetty RequestLogImpl]
           [com.codahale.metrics.jetty8 InstrumentedSelectChannelConnector
                                        InstrumentedSslSelectChannelConnector
                                        InstrumentedQueuedThreadPool
                                        InstrumentedHandler]
           [org.eclipse.jetty.server Request Server]
           [org.eclipse.jetty.server.handler RequestLogHandler HandlerCollection]
           [org.eclipse.jetty.util.component AbstractLifeCycle LifeCycle]
           [org.eclipse.jetty.util.ssl SslContextFactory]
           [org.eclipse.jetty.util.thread QueuedThreadPool ShutdownThread]))

(gen-class
 :extends org.eclipse.jetty.server.handler.AbstractHandler
 :constructors {[java.util.Map] []}
 :init init
 :state handler
 :name mixradio.adapter.RequestHandler)

(def ^:dynamic request-log-enabled?
  (Boolean/valueOf (env :requestlog-enabled "false")))

(def ^:dynamic request-log-retain-hours
  (or (when-let [hours (env :requestlog-retainhours)] (Integer/valueOf hours))
      (when-let [days (env :requestlog-retaindays)] (* 24 (Integer/valueOf days)))
      72))

(defn -init
  [col]
  [[] (:handler col)])

(defn -handle
  [this _ ^Request base-request request response]
  (let [request-map (servlet/build-request-map request)
        response-map ((.handler this) request-map)]
    (when response-map
      (servlet/update-servlet-response response response-map)
      (.setHandled base-request true))))

(defn- instrumented-proxy-handler
  "Returns a Jetty Handler implementation that is instrumented for metrics"
  [handler]
  (InstrumentedHandler. default-registry (mixradio.adapter.RequestHandler. {:handler handler})))

(defn- request-log-handler
  "A Jetty Handler that writes requests to a log file"
  []
  (System/setProperty "REQUESTLOG_RETAINHOURS" (str request-log-retain-hours))
  (let [request-log (doto (RequestLogImpl.)
                      (.setResource "/logback-access.xml"))]
    (doto (RequestLogHandler.)
      (.setRequestLog request-log))))

(defn- handlers
  "Return all the handlers for this Jetty instance"
  [handler]
  (let [handler-col (HandlerCollection.)]
    (.addHandler handler-col (instrumented-proxy-handler handler))
    (when request-log-enabled?
      (.addHandler handler-col (request-log-handler)))
    handler-col))

(defn- ssl-context-factory
  "Creates a new SslContextFactory instance from a map of options."
  [options]
  (let [context (SslContextFactory.)]
    (if (string? (options :keystore))
      (.setKeyStorePath context (options :keystore))
      (.setKeyStore context (options :keystore)))
    (.setKeyStorePassword context (options :key-password))
    (when (options :truststore)
      (.setTrustStore context (options :truststore)))
    (when (options :trust-password)
      (.setTrustPassword context (options :trust-password)))
    (case (options :client-auth)
      :need (.setNeedClientAuth context true)
      :want (.setWantClientAuth context true)
      nil)
    context))

(defn- ssl-connector
  "Creates a SslSelectChannelConnector instance."
  [options]
  (doto (InstrumentedSslSelectChannelConnector.
         default-registry
         (options :ssl-port 443)
         (ssl-context-factory options)
         (com.codahale.metrics.Clock/defaultClock))
    (.setHost (options :host))))

(defn- create-server
  "Construct a Jetty Server instance."
  [options]
  (let [connector (doto (InstrumentedSelectChannelConnector. default-registry (options :port 80) (com.codahale.metrics.Clock/defaultClock))
                    (.setHost (options :host)))
        server    (doto (Server.)
                    (.addConnector connector)
                    (.setSendDateHeader true))]
    (when (or (options :ssl?) (options :ssl-port))
      (.addConnector server (ssl-connector options)))
    server))

(defn- life-cycle
  [{:keys [on-stop]}]
  (let [l (doto (proxy [AbstractLifeCycle] []
                  (doStop []
                    (on-stop)))
            (.start))]
    (ShutdownThread/register (into-array LifeCycle [l]))))

(defn ^Server run-jetty
  "Start a Jetty webserver to serve the given handler according to the
  supplied options:

  :configurator - a function called with the Jetty Server instance
  :port         - the port to listen on (defaults to 80)
  :host         - the hostname to listen on
  :join?        - blocks the thread until server ends (defaults to true)
  :ssl?         - allow connections over HTTPS
  :ssl-port     - the SSL port to listen on (defaults to 443, implies :ssl?)
  :keystore     - the keystore to use for SSL connections
  :key-password - the password to the keystore
  :truststore   - a truststore to use for SSL connections
  :trust-password - the password to the truststore
  :max-threads  - the maximum number of threads to use (default 50)
  :client-auth  - SSL client certificate authenticate, may be set to :need,
                  :want or :none (defaults to :none)
  :on-stop      - A function to call on shutdown, before closing connectors"
  [handler options]
  (let [^Server s (create-server (dissoc options :configurator))
        ^QueuedThreadPool p (InstrumentedQueuedThreadPool. default-registry)]
    (doto p
      (.setMaxThreads (options :max-threads 254)))
    (doto s
      (.setHandler (handlers handler))
      (.setThreadPool p))
    (when-let [configurator (:configurator options)]
      (configurator s))
    (when (:on-stop options)
      (life-cycle options))
    (.start s)
    (when (:join? options true)
      (.join s))
    s))

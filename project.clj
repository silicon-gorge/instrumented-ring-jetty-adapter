(defproject mixradio/instrumented-ring-jetty-adapter "1.0.3-SNAPSHOT"

  :description "Instrumented Jetty"
  :url "https://github.com/mixradio/instrumented-ring-jetty-adapter"
  :license "https://github.com/mixradio/instrumented-ring-jetty-adapter/blob/master/LICENSE"

  :dependencies [[ch.qos.logback/logback-access "1.1.2"]
                 [com.codahale.metrics/metrics-jetty9 "3.0.2"]
                 [environ "1.0.0"]
                 [metrics-clojure "2.3.0"]
                 [org.eclipse.jetty/jetty-server "9.2.3.v20140905"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-jetty-adapter "1.3.1" :exclusions [org.eclipse.jetty/jetty-server]]
                 [ring/ring-servlet "1.3.1"]]

  :aot [mixradio.instrumented-jetty]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.6.0"]
                                       [org.slf4j/slf4j-nop "1.7.7"]]}})

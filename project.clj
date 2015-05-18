(defproject mixradio/instrumented-ring-jetty-adapter "1.0.5-SNAPSHOT"

  :description "Instrumented Jetty"
  :url "https://github.com/mixradio/instrumented-ring-jetty-adapter"
  :license "https://github.com/mixradio/instrumented-ring-jetty-adapter/blob/master/LICENSE"

  :dependencies [[ch.qos.logback/logback-access "1.1.3"]
                 [environ "1.0.0"]
                 [io.dropwizard.metrics/metrics-jetty9 "3.1.1"]
                 [metrics-clojure "2.5.1"]
                 [org.eclipse.jetty/jetty-server "9.2.9.v20150224"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2" :exclusions [org.eclipse.jetty/jetty-server]]
                 [ring/ring-servlet "1.3.2"]]

  :aot [mixradio.instrumented-jetty]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.6.0"]
                                       [org.slf4j/slf4j-nop "1.7.12"]]}})

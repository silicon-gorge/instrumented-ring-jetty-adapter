(defproject mixradio/instrumented-ring-jetty-adapter "1.0.6-SNAPSHOT"

  :description "Instrumented Jetty"
  :url "https://github.com/mixradio/instrumented-ring-jetty-adapter"
  :license "https://github.com/mixradio/instrumented-ring-jetty-adapter/blob/master/LICENSE"

  :dependencies [[ch.qos.logback/logback-access "1.1.3"]
                 [environ "1.0.0"]
                 [io.dropwizard.metrics/metrics-jetty9 "3.1.2"]
                 [metrics-clojure "2.6.1"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-servlet "1.4.0"]]

  :aot [mixradio.instrumented-jetty]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.6.0"]
                                       [org.slf4j/slf4j-nop "1.7.12"]]}})

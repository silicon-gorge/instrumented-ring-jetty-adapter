# instrumented-ring-jetty-adapter

Wraps Ring Jetty with instrumentation that collects metrics that can be sent to Graphite.

##Usage

In your project file:

```clj
[mixradio/instrumented-ring-jetty-adapter "1.0.4"]
```

[![Clojars Project](http://clojars.org/mixradio/instrumented-ring-jetty-adapter/latest-version.svg)](http://clojars.org/mixradio/instrumented-ring-jetty-adapter)

Set the following environment variables if request logging to a file is required:

- `REQUESTLOG_ENABLED` - set to true if you want web requests to be collected.
- `REQUESTLOG_RETAINHOURS` - how long to retain request log info in hours.
- `REQUESTLOG_RETAINDAYS` - how long to retain request log info in days (if `REQUESTLOG_RETAINHOURS` has been specified it will be used in preference to this value).

In the code that starts the service use something like this:

```clj
(:require [mixradio.instrumented-jetty :refer [run-jetty]])

(run-jetty {your ring handler} {:port {your service port}
                                :max-threads {number of threads to use}
                                :stacktraces? false
                                :auto-reload? false
                                :configurator {your function for extra server configuration commands}
                                :send-server-version false})
```

For a definition of the options available see the documentation of `run-jetty` in the [the code](https://github.com/mixradio/instrumented-ring-jetty-adapter/blob/master/src/mixradio/instrumented_jetty.clj).

For an example of how to use this code, use the [mr-clojure](https://github.com/mixradio/mr-clojure) 
project to `lein new mr-clojure proj` and you'll be able to to examine `proj`, a working example of a
service that uses this instrumented jetty project.

## Configuring a service for SSL

It's possible to configure a service to run https as well as http.  There are a few steps that need
to be taken to do this and you'll need to have knowledge of how SSL works and how to set it up. The
example here uses the jetty test keystore and credentials (borrowed from the jetty 9 documentation),
so don't use these in a production system!

1. Create a new project using the mr-clojure template.

2. Copy the jetty test keystore (from the root of this project) to a location on your machine,
   for example to the root of the service you just created.

2. Add the following entries to the properties defined when calling run-jetty in the start-server
   function in the `setup.clji` file of your new service:

<pre><code>:ssl-port 8443
:keystore "path-to-keystore-file"
:key-password "OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4"
:key-mgr-password "OBF:1u2u1wml1z7s1z7a1wnl1u2g"
:truststore "path-to-keystore-file"
:trust-password "OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4"
</code></pre>

 If you place the keystore file in the root of the service, you can just set path-of-keystore-file
 to be the value `keystore`. If it's in another location, specify the full path to the file.

3. Run `acceptance wait` and you'll see logging that shows both an HTTP and an HTTPS ServerConnector
   being created.

4. `curl http://localhost:8080/healthcheck` and you'll see a response over a non-SSL connection.

5. `curl -v --insecure https://localhost:8443/healthcheck` and you'll see a response over a SSL
   connection. `--insecure` means don't worry about the test certificate being invalid and `-v`
   means that you'll see the various details of SSL protocol negotiation.

## License

Copyright © 2014 MixRadio

[instrumented-ring-jetty-adapter is released under the 3-clause license ("New BSD License" or "Modified BSD License")](https://github.com/mixradio/instrumented-ring-jetty-adapter/blob/master/LICENSE).

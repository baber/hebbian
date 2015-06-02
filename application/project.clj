(defproject hebbian "0.0.1-SNAPSHOT"
            :description "Hebbian user application."
            :dependencies [[org.clojure/clojure "1.7.0-beta3"]
                           [org.clojure/clojurescript "0.0-3269"]
                           [com.cemerick/piggieback "0.2.0"]
                           [org.clojure/tools.nrepl "0.2.10"]
                           ;[lein-light-nrepl "0.0.13"]
                           [prismatic/dommy "1.1.0"]
                           [cljsjs/moment "2.9.0-0"]
                           [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                           [compojure "1.3.4"]
                           [clj-time "0.9.0"]
                           [cljsjs/react "0.13.3-0"]]



            :repl-options {
                           :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
            :plugins [[lein-cljsbuild "1.0.6"]
                      [lein-ring "0.9.3"]]

            ; Enable the lein hooks for: clean, compile, test, and jar.
            :hooks [leiningen.cljsbuild]


            :ring {:handler com.infimany.hebbian.app.handlers/app}

            :cljsbuild {
                        :builds [{
                                  :source-paths ["src"]
                                  :compiler     {:output-dir    "resources/public/js"
                                                 :output-to     "resources/public/js/hebbian-debug.js"
                                                 :optimizations :none
                                                 :pretty-print  true
                                                 :source-map    true
                                                 :foreign-libs  [{:file "react-0.13.3.js"
                                                                  :provides ["React.DOM"]}
                                                                 {:file "momentjs-2.10.13.js"
                                                                  :provides ["moment"]}
                                                                 ]}
                                  }]}

            :clean-targets ^{:protect false} ["resources/public/js"]

            :profiles {:dev {
                             :dependencies
                             [[javax.servlet/servlet-api "2.5"]]}}

            )


(defproject app-pedestal "0.0.1-SNAPSHOT"
  :description "Hebbian user application."
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [com.cemerick/piggieback "0.1.0"]
                 [lein-light-nrepl "0.0.13"]
                 [prismatic/dommy "0.1.2"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [compojure "1.1.6"]
                 [clj-time "0.6.0"]]



  :repl-options  {
                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl lighttable.nrepl.handler/lighttable-ops]}
  :plugins [[lein-cljsbuild "1.0.2"]
            [lein-ring "0.8.10"]]

  ; Enable the lein hooks for: clean, compile, test, and jar.
  :hooks [leiningen.cljsbuild]

  :ring {:handler com.infimany.hebbian.app.handlers/app}

  :cljsbuild {
              :builds [{
                        :source-paths ["src"]
                        :compiler {:output-dir "resources/public/js"
                                   :output-to "resources/public/js/hebbian-debug.js"
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :source-map "resources/public/js/hebbianb-debug.js.map"
                                   :foreign-libs [{:file "react-0.8.0.js"
                                                   :provides  ["React.DOM"]}
                                                  {:file "momentjs-2.5.1.js"
                                                   :provides ["moment"]}
                                                  ]}
                        }]})


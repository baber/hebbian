( defproject events "0.1.0-SNAPSHOT"
  :description "Events service for Hebbian application."
  :url "http://www.infimany.com/hebbian"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.novemberain/monger "1.7.0"]
                 [bigml/closchema "0.5"]
                 [cheshire "5.3.1"]
                 [lein-light-nrepl "0.0.13"]
                 [compojure "1.1.6"]
                 [slingshot "0.10.3"]
                 [ring/ring-devel "1.2.1"]
                 [ring/ring-json "0.2.0"]
                 [ring-mock "0.1.5"]
                 [com.infimany.hebbian.services/common "0.1.0-SNAPSHOT"]]

  :repositories [["snapshots" "http://localhost:8081/nexus/content/repositories/snapshots"]
                 ["releases" "http://localhost:8081/nexus/content/repositories/releases"]]


  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler com.infimany.hebbian.event-service.rest.handlers/app}

  :profiles {:dev {:resource-paths ["test-resources"]
                   :dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}}

  :repl-options {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]})

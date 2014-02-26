( defproject com.infimany.hebbian.services/common "0.1.0-SNAPSHOT"
  :description "Common utilities for backend Hebbian services."
  :url "http://www.infimany.com/hebbian"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.novemberain/monger "1.7.0"]
                 [bigml/closchema "0.5"]
                 [cheshire "5.3.1"]
                 [compojure "1.1.6"]
                 [slingshot "0.10.3"]
                 [ring/ring-json "0.2.0"]]

  :deploy-repositories [["snapshots" "http://localhost:8081/nexus/content/repositories/snapshots"]
                        ["releases" "http://localhost:8081/nexus/content/repositories/releases"]]

  :repl-options {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]}

 :profiles {:dev {:resource-paths ["test-resources"]}}

)

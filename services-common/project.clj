( defproject com.infimany.hebbian.services/common "0.1.0-SNAPSHOT"
  :description "Common utilities for backend Hebbian services."
  :url "http://www.infimany.com/hebbian"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.novemberain/monger "2.0.1"]
                 [bigml/closchema "0.5"]
                 [cheshire "5.4.0"]
                 [compojure "1.3.4"]
                 [slingshot "0.12.2"]
                 [ring/ring-json "0.3.1"]]

  :deploy-repositories [["snapshots" "http://localhost:8081/nexus/content/repositories/snapshots"]
                        ["releases" "http://localhost:8081/nexus/content/repositories/releases"]]

 :profiles {:dev {:resource-paths ["test-resources"]}}

)

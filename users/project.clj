(defproject users "0.1.0-SNAPSHOT"
  :description "User management service for Hebbian application."
  :url "http://www.infimany.com/hebbian"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.novemberain/monger "2.0.1"]
                 [bigml/closchema "0.5"]
                 [cheshire "5.4.0"]
                 ;              [lein-light-nrepl "0.0.13"]
                 [compojure "1.3.4"]
                 [slingshot "0.12.2"]
                 [ring/ring-devel "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-mock "0.2.0"]
                 [com.infimany.hebbian.services/common "0.1.0-SNAPSHOT"]]

  :repositories [["snapshots" "http://localhost:8081/nexus/content/repositories/snapshots"]
                 ["releases" "http://localhost:8081/nexus/content/repositories/releases"]]


  :plugins [[lein-ring "0.9.3"]]
  :ring {:handler com.infimany.hebbian.user-service.rest.handlers/app}

  :profiles {:dev {
                   :resource-paths ["test-resources"]
                   :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9999"]
                   :dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.2.0"]]}}

  :aliases {"debug" ["with-profile" "dev" ["ring" "serve"]]}

            ; :repl-options {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]}
  )

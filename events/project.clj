(defproject events "0.1.0-SNAPSHOT"
            :description "Events service for Hebbian application."
            :url "http://www.infimany.com/hebbian"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}


            :dependencies [[org.clojure/clojure "1.6.0"]
                           [com.novemberain/monger "2.0.1"]
                           [bigml/closchema "0.5"]
                           [cheshire "5.4.0"]
                           [compojure "1.3.4"]
                           [slingshot "0.12.2"]
                           [ring/ring-devel "1.3.2"]
                           [ring/ring-json "0.3.1"]
                           [ring/ring-mock "0.2.0"]
                           [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                           [compojure "1.3.4"]
                           [clj-time "0.9.0"]
                           [clj-http "1.1.2"]
                           [com.infimany.hebbian.services/common "0.1.0-SNAPSHOT"]
                           ]


            :repositories [["snapshots" "http://localhost:8081/nexus/content/repositories/snapshots"]
                           ["releases" "http://localhost:8081/nexus/content/repositories/releases"]]


            :plugins [[lein-ring "0.9.3"]]
            :ring {:handler com.infimany.hebbian.event-service.rest.handlers/app}

            :profiles {:dev {:resource-paths ["test-resources"]
                             :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9998"]
                             :dependencies   [[javax.servlet/servlet-api "2.5"]
                                              [ring/ring-mock "0.2.0"]]}}

            )

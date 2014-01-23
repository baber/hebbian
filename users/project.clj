( defproject users "0.1.0-SNAPSHOT"
  :description "Real time offers platform taking into account location and time.  Uses Hebbian
learning principles to ensure users are only shown offers they are likely to respond to."
  :url "http://www.infimany.com/hebbian"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.novemberain/monger "1.5.0"]
                 [com.novemberain/monger "1.5.0"]
                 [bigml/closchema "0.5"]
		 [cheshire "5.3.1"]]

  :profiles {:dev {:resource-paths ["test-resources"] }} 
)

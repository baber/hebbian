(ns com.infimany.hebbian.event-service.notifications.new-events
  (:require [clojure.core]
            [cheshire.core :refer [generate-string]]
            [clj-time.core :refer [now days]]
            [clojure.core.async :as async :refer [<!! timeout]]
            )

  (:import [org.bson.types ObjectId])
)

(def origin {:lat 51.734262 :lng -0.455852})
(def interval 5000)

(def base-event
  {
   :_id (str (ObjectId.))
   :details "A party!!"
   :start-time (.toDate (now))
   :geolocation {:type "Point" :coordinates [(:lng origin) (:lat origin)]}
   :location {:postalCode "TEST" :country "TEST"}
   }
  )

(defn get-new-events []
  (<!! (timeout interval))
  (println "Generating new event!")
  (generate-string base-event)
  )



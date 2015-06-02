(ns com.infimany.hebbian.event-service.notifications.new-events
  (:require [cheshire.core :refer [generate-string]]
            [closchema.core :as schema]
            [clojure.java.io :as io]
            [clojure.java.io :refer [resource]]
            [com.infimany.hebbian.event-service.db.events :refer [insert-event get-events delete-events]]
            [clj-time.core :refer [now days]]
            [clj-time.periodic :refer [periodic-seq]]
            [clj-time.format :as time-fmt]
            [clojure.core.async :as async :refer [<!! timeout]]
            )

  (:use [clojure.string :only [split trim]])

  (:import [org.bson.types ObjectId])
)

(def origin {:lat 54.702355, :lng -3.276575})
(def max-lng-shift 0.2)
(def max-lat-shift 0.1)
(def interval 20000)


(defn round [scale number]
  "Quick and dirty rounding function - NOT general purpose as it returns a double so will be inaccurate if scale is too high for a double."
  (double (.setScale (bigdec number) scale java.math.RoundingMode/HALF_EVEN)))

(def summaries (flatten (repeat 3 (map #(trim %) (split (slurp (resource "./events.txt")) #",")))))


(defn calculate-delta [limit]
  (let [delta (rand limit)]
    (if (> delta (/ limit 2)) delta
      (* -1 delta))
    )
  )


(defn move-location [location]
  {:lat (round 6 (+ (calculate-delta max-lat-shift) (:lat origin)))  :lng (round 6 (+ (calculate-delta max-lng-shift) (:lng origin)))}
)



(defn create-event [details start-time geolocation]
  {
   :_id (str (ObjectId.))
   :details details
   :start-time start-time
   :geolocation {:type "Point" :coordinates [(:lng geolocation) (:lat geolocation)]}
   :location {:postalCode "test" :country "UK"}
   }
  )


(def events
  (map create-event summaries (periodic-seq (now) (days 3)) (iterate move-location origin) ) )



(defn get-new-event []
  (<!! (timeout interval))
  (str "data: " (generate-string (rand-nth events)) "\n\n")
  )





(ns events.data-setup
  (:require [cheshire.core :refer [parse-string]]
            [closchema.core :as schema]
            [clojure.java.io :as io]
            [clojure.java.io :refer [resource]]
            [com.infimany.hebbian.event-service.db.events :refer [insert-event get-events delete-events]]
            [com.infimany.hebbian.services.common.db :as db-common]
            [clj-time.core :refer [now days]]
            [clj-time.periodic :refer [periodic-seq]]
            [clj-time.format :as time-fmt]
            )

  (:use [clojure.string :only [split trim
                               ]])

  (:import [org.bson.types ObjectId])


  )

(def origin {:lat 51.734262 :lng -0.455852})
(def max-lng-shift 0.2)
(def max-lat-shift 0.1)

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
   :location {:postalCode "TEST" :country "TEST"}
   }
  )


(defn generate-events []
  (map create-event summaries (periodic-seq (now) (days 3)) (iterate move-location origin) )
)

(defn insert-test-events []
  (delete-events)
  (doseq [event (generate-events)]
    (db-common/insert event  "events")
    ))


(insert-test-events)
;(get-all-events)





(ns com.infimany.hebbian.event-service.db.events
  (:require [clojure.core]
            [cheshire.core :refer [parse-string]]
            [monger.core]
            [monger.json]
            [monger.operators :refer :all]
            [monger.collection :as monger-coll]
            [closchema.core :as schema]
            [clojure.java.io :as io]
            [com.infimany.hebbian.services.common.db :as db-common]
            [com.infimany.hebbian.services.common.validation :as validation]
            [com.infimany.hebbian.event-service.geocode-utils :as geo-utils]
            [clj-time.format :as time-fmt]
            [monger.joda-time]
            )

  (:use [slingshot.slingshot :only [throw+]])

  (:import [com.mongodb MongoOptions ServerAddress])
  (:import [java.io File])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern])

)

(def time-formatter (time-fmt/formatter "YYYY-MM-dd'T'HH:mm:ssZZ"))

(def collection-name "events")
(def earth-radius 6371)

(db-common/initialise-db "test")

(defn get-events [criteria]
  (let [start-time (:start-time criteria) end-time (:end-time criteria)]
    (let [query
          {:geolocation {"$geoWithin"
                         { "$centerSphere"
                           [ [ (:lng criteria) , (:lat criteria) ] (/ (:distance criteria) earth-radius) ]
                           } } }]
      (let [time-bounds (into {} (for [[k operator] [[:start-time "$gte"] [:end-time "$lt"]] :when (k criteria)] [operator (k criteria)]))]
        (monger-coll/find-maps collection-name
                               (if (= 0 (count time-bounds)) query (merge query {:start-time time-bounds}) )
                               ))

      ) )
  )

;;  (count (get-events {:distance 2 :lat 51.734262  :lng -0.455852
;;                      :start-time (time-fmt/parse time-formatter "2014-03-07T00:00:00+00:00")
;;                      :end-time (time-fmt/parse time-formatter "2014-10-19T00:00:00+00:00")
;;                      }))

(defn parse-date [datestring]
  (time-fmt/parse time-formatter datestring)
)

(defn insert-event [event]
  (let [geo-loc (geo-utils/geocode event)]
    (if (= nil geo-loc) (throw+ {:type :invalid_json :message (str "Geo location could not be determined from supplied address: " (:location event))} ) )
    (let [data (assoc event :geolocation geo-loc) errors (validation/validate-json data "event-v1.json")]
      (cond
       (> (count errors) 0) (throw+ {:type :invalid_json :message (str "JSON is not valid" errors )} )
       :else (db-common/insert (update-in data [:start-time :end-time] parse-date) collection-name))))
  )


(defn delete-events []
  (monger-coll/remove collection-name ))


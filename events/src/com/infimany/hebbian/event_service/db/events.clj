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
            [com.infimany.hebbian.event-service.geocode-utils :as geo-utils]
            [clj-time.format :as time-fmt]
            [monger.joda-time]
)

  (:use [slingshot.slingshot :only [throw+]])

  (:import [com.mongodb MongoOptions ServerAddress])
  (:import [java.io File])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

(def time-formatter (time-fmt/formatter "YYYY-MM-dd"))

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

 (count (get-events {:distance 3 :lat 51.734262  :lng -0.455852
                     :start-time (.toDate (time-fmt/parse time-formatter "2014-02-06"))
                     :end-time (.toDate (time-fmt/parse time-formatter "2015-02-06"))
                     }))

(defn insert-event [event]
  (let [geo-loc (geo-utils/geocode event)]
    (if (= nil geo-loc) (throw+ {:type :invalid_json :message (str "Geo location could not be determined from supplied address: " (:location event))} ) )
    (db-common/insert (assoc event :geolocation geo-loc) "event-v1.json" collection-name))
  )


(defn delete-events []
  (monger-coll/remove collection-name ))


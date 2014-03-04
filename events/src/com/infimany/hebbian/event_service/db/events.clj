(ns com.infimany.hebbian.event-service.db.events
  (:require [clojure.core]
            [cheshire.core :refer [parse-string]]
            [monger.core]
            [monger.json]
            [monger.collection :as monger-coll]
            [closchema.core :as schema]
            [clojure.java.io :as io]
            [com.infimany.hebbian.services.common.db :as db-common]
            [com.infimany.hebbian.event-service.geocode-utils :as geo-utils])

  (:use [slingshot.slingshot :only [throw+]])

  (:import [com.mongodb MongoOptions ServerAddress])
  (:import [java.io File])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))


(def collection-name "events")

(db-common/initialise-db "test")

(defn get-events [postcode distance]
  (println postcode distance)
  (monger-coll/find-maps collection-name)
  )


(defn insert-event [event]
  (let [geo-loc (geo-utils/geocode event)]
    (if (= nil geo-loc) (throw+ {:type :invalid_json :message (str "Geo location could not be determined from supplied address: " (:location event))} ) )
    (db-common/insert (assoc event :geolocation geo-loc) "event-v1.json" collection-name))
  )


(defn delete-events []
  (monger-coll/remove collection-name ))


(ns com.infimany.hebbian.event-service.db.events
  (:require [clojure.core]
            [cheshire.core :refer [parse-string]]
            [monger.core]
            [monger.json]
            [monger.collection :as monger-coll]
            [closchema.core :as schema]
            [clojure.java.io :as io]
            [com.infimany.hebbian.services.common.db :as db-common])

  (:use [slingshot.slingshot :only [throw+]])

  (:import [com.mongodb MongoOptions ServerAddress])
  (:import [java.io File])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))


(def collection-name "events")

(db-common/initialise-db "test")

(defn get-all-events []
  (monger-coll/find-maps collection-name)
  )


(defn insert-event [event]
  (db-common/insert event "event-v1.json" collection-name)
  )


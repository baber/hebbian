(ns com.infimany.hebbian.event-service.db.admin
  (:require [clojure.core]
            [monger.core]
            [monger.collection :as monger-coll]))


(def db-name "test")


(defn create-indices []
  (monger.core/connect!)
  (monger.core/set-db! (monger.core/get-db db-name))
  (monger-coll/ensure-index "events" (array-map :geolocation "2dsphere") )
  (monger-coll/ensure-index "events" (array-map :start-time 1) )
  (monger.core/disconnect!)
  )



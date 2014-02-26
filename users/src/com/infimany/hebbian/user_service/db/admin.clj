(ns com.infimany.hebbian.user-service.db.admin
  (:require [clojure.core]
            [monger.core]
            [monger.collection :as monger-coll]))


(def db-name "test")


(defn create-indices []
  (monger.core/connect!)
  (monger.core/set-db! (monger.core/get-db db-name))
  ;(monger-coll/ensure-index "users" (array-map :identity-id 1) { :unique true })
  (monger.core/disconnect!)
  )


(create-indices)

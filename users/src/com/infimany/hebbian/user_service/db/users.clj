(ns com.infimany.hebbian.user-service.db.users
  (:require [clojure.core]
            [monger.core :as monger]
            [monger.collection :as monger-coll]
            )

  (:use [slingshot.slingshot :only [throw+]])

)


(def collection-name "users")
(def db-name "hebbian")

(defn get-user [id]
  (let [conn (monger/connect)
        db (monger/get-db conn db-name)]
    (monger-coll/find-one-as-map db collection-name {:_id id})))

(defn insert [data]
  (let [conn (monger/connect)
        db (monger/get-db conn db-name)]
    (cond
      (empty? data) (throw+ {:type :invalid_json :message "JSON data is empty"})
      :else (monger-coll/update db collection-name {:_id (:_id data)} data {:upsert true}))))

(defn insert-user [user]
  (insert user)
  )

(defn delete-user [id]
  (let [conn (monger/connect) db (monger/get-db conn db-name)]
    (monger-coll/remove db collection-name {:_id id}))
  )

;(insert-user {:name "bob"})


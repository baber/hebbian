(ns com.infimany.hebbian.user-service.db.users
  (:require [clojure.core]
            [monger.core]
            [monger.collection :as monger-coll]
            [com.infimany.hebbian.services.common.db :as db-common]
            )

)


(def collection-name "users")

(db-common/initialise-db "test")

(defn get-user [id]
  (monger-coll/find-one-as-map collection-name {:_id id}) )


(defn insert-user [user]
  (db-common/insert user "user-v1.json" collection-name)
  )

(defn delete-user [id]
  (monger-coll/remove collection-name {:_id id}))


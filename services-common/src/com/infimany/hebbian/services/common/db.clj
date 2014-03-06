; Some common utilities for mongo access.

(ns com.infimany.hebbian.services.common.db
  (:require
            [monger.core]
            [monger.collection :as monger-coll]
            )

 (:use [slingshot.slingshot :only [throw+]])

)


(defn initialise-db [db-name]
  (monger.core/connect!)
  (monger.core/set-db! (monger.core/get-db db-name))
)

(defn shutdown-db []
  (monger.core/disconnect!)
)

(defn insert [data collection]
  (cond
   (empty? data) (throw+ {:type :invalid_json :message "JSON data is empty"} )
   :else (monger-coll/update collection {:_id (:_id data)} data :upsert true)
  ) )


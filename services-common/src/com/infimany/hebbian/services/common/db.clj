; Some common utilities for mongo access.

(ns com.infimany.hebbian.services.common.db
  (:require [cheshire.core :refer [parse-string]]
            [closchema.core :as schema]
            [clojure.java.io :refer [resource reader file]]
            [monger.core]
            [monger.collection :as monger-coll]
            )

 (:use [slingshot.slingshot :only [throw+]])

)


(defn load-schema [name]
  (parse-string (slurp (reader (resource (str "schemas/" name)))) true)
  )

(def get-schema (memoize load-schema))


(defn validate-json [data schema]
  (schema/validate (get-schema schema) data )
  )

(defn initialise-db [db-name]
  (monger.core/connect!)
  (monger.core/set-db! (monger.core/get-db db-name))
)

(defn shutdown-db []
  (monger.core/disconnect!)
)

(defn insert [data schema collection]
  (cond
   (empty? data) (throw+ {:type :invalid_json :message "JSON data is empty"} )
   (validate-json data schema) (monger-coll/update collection {:_id (:_id data)} data :upsert true)
   :else (throw+ {:type :invalid_json :message (str "JSON is not valid" (schema/report-errors (validate-json data schema)) )} ) )
  )


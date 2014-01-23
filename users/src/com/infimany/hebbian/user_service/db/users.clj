(ns com.infimany.hebbian.user-service.db.users
  (:require [clojure.core])
  (:require [cheshire.core :refer [parse-string]])
  (:require [monger.core])
  (:require [monger.collection :as monger-coll])
  (:require [closchema.core :as schema])
  (:require [clojure.java.io :as io])
  
  (:import [com.mongodb MongoOptions ServerAddress])
  (:import [java.io File])  
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))



(def schemas-path "/Users/baberkhalil/software/git_repos/hebbian/users/resources/schemas")

(def schemas
  (let [schema-files  (filter #(.endsWith (.getName %) ".json")  (file-seq (io/as-file schemas-path)))]
    (zipmap (map #(keyword (.getName %)) schema-files) (map #(parse-string (slurp %)) schema-files))
    ) 
  )

; db related functions

;(monger.core/connect!)

;(monger.core/set-db! (monger.core/get-db "test"))

(defn get-user [id]
    (monger-coll/find-maps "users")
    )

(defn insert-user [user]
  (monger-coll/insert "users" user))



; json validation functions

(defn validate-json [data schema]
   (schema/validate ((keyword schema) schemas)  data )
  )













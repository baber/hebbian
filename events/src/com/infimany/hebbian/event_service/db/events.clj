(ns com.infimany.hebbian.event-service.db.events
  (:require [clojure.core])
  (:require [cheshire.core :refer [parse-string]])
  (:require [monger.core])
  (:require [monger.json])
  (:require [monger.collection :as monger-coll])
  (:require [closchema.core :as schema])
  (:require [clojure.java.io :as io])
  (:use [slingshot.slingshot :only [throw+]])

  (:import [com.mongodb MongoOptions ServerAddress])
  (:import [java.io File])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))



(def schemas-path "/Users/baberkhalil/software/git_repos/hebbian/events/resources/schemas")

(def schemas
  (let [schema-files  (filter #(.endsWith (.getName %) ".json")  (file-seq (io/as-file schemas-path)))]
    (zipmap (map #(keyword (.getName %)) schema-files) (map #(parse-string (slurp %) true) schema-files))
    )
  )



; db setup related functions

(monger.core/connect!)

(monger.core/set-db! (monger.core/get-db "test"))

; json validation functions

(defn validate-json [data schema]
  (schema/validate ((keyword schema) schemas) data )

  )



; db data access functions

(defn get-all-events []
  (monger-coll/find-maps "events")
    )


(defn insert-event [event]
  (cond
   (empty? event) (throw+ {:type :invalid_json :message "Posted JSON is empty"} )
   (validate-json event "event-v1.json") (monger-coll/update "events" {:_id (:_id event)} event :upsert true)
   :else (throw+ {:type :invalid_json :message (str "Posted JSON is not valid" (schema/report-errors (validate-json event "event-v1.json")) )} ) )
  )


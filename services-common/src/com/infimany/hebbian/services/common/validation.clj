(ns com.infimany.hebbian.services.common.validation
  (:require [cheshire.core :refer [parse-string]]
            [closchema.core :as schema]
            [clojure.java.io :refer [resource reader file]]
            )

)


(defn load-schema [name]
  (parse-string (slurp (reader (resource (str "schemas/" name)))) true)
  )

(def get-schema (memoize load-schema))


(defn validate-json [data schema]
  (schema/report-errors (schema/validate (get-schema schema) data ))
  )


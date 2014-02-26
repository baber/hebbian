(ns events.data-setup
  (:require [cheshire.core :refer [parse-string]]
            [closchema.core :as schema]
            [clojure.java.io :as io]
            [clojure.java.io :refer [resource]]
            [com.infimany.hebbian.event-service.db.events :refer [insert-event get-all-events]]
            )
)


; set up some test data.

(defn insert-test-events []
  (doseq [event (parse-string (slurp (resource "./events.json")) true )]
    (insert-event event)
    ))

;(insert-test-events)
;(get-all-events)





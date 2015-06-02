(ns events.data-setup
  (:require
    [com.infimany.hebbian.event-service.notifications.new-events :refer [events]]
    [com.infimany.hebbian.event-service.db.events :refer [delete-events]]
    [monger.core :as monger])
  )

(def collection-name "events")
(def db-name "hebbian")

(defn insert-test-events []
  (delete-events)
  (let [conn (monger/connect) db (monger/get-db conn db-name)]
    (doseq [event events]
      (monger.collection/insert db collection-name event)
      )))


(insert-test-events)
;(get-all-events)





(ns events.data-setup
  (:require
   [com.infimany.hebbian.event-service.notifications.new-events :refer [events]]
   [com.infimany.hebbian.event-service.db.events :refer [delete-events]]
   [com.infimany.hebbian.services.common.db :as db-common]
   )
  )



(defn insert-test-events []
  (delete-events)
  (doseq [event events]
    (db-common/insert event  "events")
    ))


;(insert-test-events)
;(get-all-events)





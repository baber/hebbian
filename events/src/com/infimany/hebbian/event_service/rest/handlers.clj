(ns com.infimany.hebbian.event-service.rest.handlers
  (:use
   [compojure.core]
   [clojure.string :only [blank?]]
   )

  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.stacktrace :as ring-stacktrace]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as response]
            [com.infimany.hebbian.event-service.db.events :refer [get-events insert-event]]
            [com.infimany.hebbian.event-service.notifications.new-events :refer [get-new-event]]
            [com.infimany.hebbian.event-service.geocode-utils :refer [geocode]]
            [com.infimany.hebbian.services.common.exceptions :as exceptions-common]
            [com.infimany.hebbian.services.common.ring-handlers :as handlers-common]
            [cheshire.core :refer [parse-string]]
            [clj-time.format :as time-fmt]
            ) )


(def time-formatter (time-fmt/formatter "YYYY-MM-dd'T'HH:mm:ssZZ"))

(defn extract-params [query-params]
  (merge
   (into {} (for [k [:distance :lat :lng] :when (not (blank? (k query-params)))] [k (read-string (k query-params))]))
   (into {} (for [k [:start-time :end-time] :when (not (blank? (k query-params)))] [k (time-fmt/parse time-formatter (k query-params))]))
  ) )

(defroutes event-routes
  (GET "/event" {query-params :params} {:body (get-events  (extract-params query-params))})
  (GET "/event/geocode" {{postcode :postcode} :params} {:body (geocode {:location {:postalCode postcode :country "UK"}})})
  (GET "/event/updates" [] {:headers {"content-type" "text/event-stream;charset=UTF-8"} :body (get-new-event)})
  (POST "/event" {event :body} (insert-event event) {:body ""})
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api event-routes)
      (ring-json/wrap-json-response)
      (ring-json/wrap-json-body {:keywords? true})
      (exceptions-common/wrap-exceptions)
      (handlers-common/cross-domain-access)))



(ns com.infimany.hebbian.event-service.rest.handlers
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.stacktrace :as ring-stacktrace]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as response]
            [com.infimany.hebbian.event-service.db.events :refer :all]
            [com.infimany.hebbian.event-service.geocode-utils :refer [geocode]]
            [com.infimany.hebbian.services.common.exceptions :as exceptions-common]
            [com.infimany.hebbian.services.common.ring-handlers :as handlers-common]
            [cheshire.core :refer [parse-string]]) )



(defroutes event-routes
  (GET "/event" {{lat :lat lng :lng distance :distance} :params} {:body (get-events {:lng (read-string lng) :lat (read-string lat)} (read-string distance))})
  (GET "/event/geocode" {{postcode :postcode} :params} {:body (geocode {:location {:postalCode postcode :country "UK"}})})
  (POST "/event" {event :body} (insert-event event) {:body ""})
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api event-routes)
      (ring-json/wrap-json-response)
      (ring-json/wrap-json-body {:keywords? true})
      (exceptions-common/wrap-exceptions)
      (handlers-common/cross-domain-access)))



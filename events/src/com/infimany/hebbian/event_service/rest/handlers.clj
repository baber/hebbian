(ns com.infimany.hebbian.event-service.rest.handlers
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.stacktrace :as ring-stacktrace]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as response]
            [com.infimany.hebbian.event-service.db.events :refer :all]
            [com.infimany.hebbian.event-service.rest.exceptions :refer :all]
            [cheshire.core :refer [parse-string]]) )



(defroutes event-routes
  (GET "/event" [] {:body (get-all-events)})
  (route/resources "/")
  (route/not-found "Not Found"))


(defn access-control [handler]
  (fn [request]
    (-> (handler request)
        (response/header "Access-Control-Allow-Origin" "*")
        )) )

(def app
  (-> (handler/api event-routes)
      (ring-json/wrap-json-response)
      (ring-json/wrap-json-body {:keywords? true})
      (wrap-exceptions)
      (access-control)))



(ns com.infimany.hebbian.user-service.rest.handlers
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.stacktrace :as ring-stacktrace]
            [ring.middleware.json :as ring-json]
            [com.infimany.hebbian.user-service.db.users :refer :all]
            [com.infimany.hebbian.user-service.rest.exceptions :refer :all]))

(defroutes user-routes
                (GET "/user/:id" [id] {:body (get-user id)})
                (POST "/user" {user :body} (insert-user user) {:body ""})
                (route/resources "/")
                (route/not-found "Not Found"))


(def app
  (-> (handler/api user-routes)
      (ring-json/wrap-json-response)
      (ring-json/wrap-json-body {:keywords? true})
      (wrap-exceptions)))



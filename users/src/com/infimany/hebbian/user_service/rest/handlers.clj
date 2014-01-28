(ns com.infimany.hebbian.user-service.rest.handlers
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [com.infimany.hebbian.user-service.db.users :refer :all]))

(defroutes app-routes
  (GET "/user/:id" [id] (json/generate-string (get-user id)))
  (route/resources "/")
  (route/not-found "Not Found"))



(def app
  (handler/site app-routes))

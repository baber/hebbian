(ns app-pedestal.handlers
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]) )



(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (handler/api app-routes))





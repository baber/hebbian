(ns com.infimany.hebbian.app.routes
  (:use compojure.core
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [com.infimany.hebbian.app.view :as view]))

(defroutes main-routes
  (GET "/" [] (view/index-page))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))
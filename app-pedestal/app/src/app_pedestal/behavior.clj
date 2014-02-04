(ns ^:shared app-pedestal.behavior
  (:require
   [clojure.string :as string]
   [io.pedestal.app.messages :as msg]
   [io.pedestal.app :as app]))


(defn user-update [old-value new-value]
  (:value new-value))

(defn init-main [_]
  [[:transform-enable [:main :user-details] :user [{msg/topic [:enable-button]}]]])

(def example-app
  {:version 2
   :transform [[:user [:new-details] user-update]]
   :emit [{:init init-main}
          [#{[:*]} (app/default-emitter [:main])]]})


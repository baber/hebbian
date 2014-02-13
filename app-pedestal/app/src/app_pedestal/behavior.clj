(ns ^:shared app-pedestal.behavior
  (:require
   [clojure.string :as string]
   [io.pedestal.app.messages :as msg]
   [io.pedestal.app :as app]))


(defn update-profile [old-value new-value]
    (:value new-value))

(defn refresh-profile [old-value new-value]
  (:value new-value))

(defn init-main [_]
  [[:transform-enable [:main :user-profile] :refresh [{msg/topic [:enable-button]}]]])

(defn publish-user-details-effects [msg]
    [(:message msg)]
  )

(def example-app
  {:version 2
   :transform [[:update [:user-profile] update-profile] [:refresh [:user-profile] refresh-profile]]
   :effect #{[#{[:user-profile]} publish-user-details-effects :default]}
   :emit [{:init init-main}
          [#{[:*]} (app/default-emitter [:main])]]})


(ns ^:shared app-pedestal.behavior
  (:require
   [clojure.string :as string]
   [io.pedestal.app.messages :as msg]
   [io.pedestal.app :as app]))


(defn update-profile [old-value new-value]
  (.log js/console (pr-str "Update Profile Called!!!!"))
  (:value new-value))

(defn refresh-profile [old-value new-value]
  (:value new-value))

(defn init-main [_]
  [[:transform-enable [:main :user-profile] :refresh [{msg/topic [:enable-button]}]]])

(defn publish-effects [user-details]
  ;;(.log js/console (pr-str "Publish Effects Called: " user-details))
  [{msg/type :update msg/topic [:user-profile] :value (:value (:message user-details))}]
  )

(def example-app
  {:version 2
   :transform [[:update [:user-profile] update-profile] [:refresh [:user-profile] refresh-profile]]
   :effect #{[#{[:user-profile]} publish-effects :single-va]}
   :emit [{:init init-main}
          [#{[:*]} (app/default-emitter [:main])]]})


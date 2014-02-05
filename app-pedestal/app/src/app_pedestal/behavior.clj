(ns ^:shared app-pedestal.behavior
  (:require
   [clojure.string :as string]
   [io.pedestal.app.messages :as msg]
   [io.pedestal.app :as app]))


(defn user-update [old-value new-value]
  (.log js/console (pr-str "User Update Called!!!!"))
  (:value new-value))

(defn active-user [old-value new-value]
  (:value new-value))

(defn init-main [_]
  [[:transform-enable [:main :user-details] :user [{msg/topic [:enable-button]}]]])

(defn publish-effects [identity-id]
  (.log js/console (pr-str "Publish Effects Called!!!!"))
  [{msg/type :user msg/topic [:refresh] :value identity-id}]
  )

(def example-app
  {:version 2
   :transform [[:user [:user-details] user-update] [:user [:active-user] active-user]]
   :effect #{[#{[:active-user]} publish-effects :single-val]}
   :emit [{:init init-main}
          [#{[:*]} (app/default-emitter [:main])]]})


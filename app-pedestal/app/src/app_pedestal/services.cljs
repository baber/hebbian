(ns app-pedestal.services
  (:require [io.pedestal.app.protocols :as p]
            [cljs.reader :as reader]
            [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan close! <! >!]]
            [io.pedestal.app.messages :as msg])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))


(defn get-user [input-queue]
  (xhr/send "http://localhost:3000/user/123456"
            (fn [event]
              (let [res (js->clj (-> event .-target .getResponseJson) :keywordize-keys true)]
                (p/put-message input-queue
                               {msg/type :refresh msg/topic [:user-profile] :value res}
                               )))))


(defn save-user-profile [message]
  (.log js/console (pr-str "Save user profile: " message))
  (xhr/send "http://localhost:3000/user"
            (fn [event]
              (let [response (-> event .-target)]
                (if (not (= 200 (.getStatus response)))
                  (.log js/console (str "Failed to post user profile!  Server response: " (.getResponseText response))))) )
              "POST" (JSON.stringify (clj->js (:value message)))
              (clj->js {"content-type" "application/json"})
              ) )



(defn services-fn [message input-queue]
  (save-user-profile message))


(ns app-pedestal.services
  (:require [io.pedestal.app.protocols :as p]
            [cljs.reader :as reader]
            [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan close! <! >!]]
            [io.pedestal.app.messages :as msg])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))


(defn get-user [message input-queue]
  (xhr/send (str "http://localhost:3000/user/" (:value message))
            (fn [event]
              (let [res (js->clj (-> event .-target .getResponseJson) :keywordize-keys true)]
                (p/put-message input-queue
                               {msg/type :user msg/topic [:user-details] :value res}
                               )))))

(defn services-fn [message input-queue]
  (get-user message input-queue))


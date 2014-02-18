(ns com.infimany.hebbian.app.services
  (:require
            [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan close! <! >!]]
            )
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))


(defn get-user [chan]
  (xhr/send "http://localhost:3000/user/1"
            (fn [event]
              (let [res (js->clj (-> event .-target .getResponseJson) :keywordize-keys true)]
                (.log js/console (pr-str "Getting user in service: "  chan) ) (go (>! chan res))))))


(defn save-user-profile [user-profile]
  (.log js/console (pr-str "Saving user profile!!!!") )
  (xhr/send "http://localhost:3000/user"
            (fn [event]
              (let [response (-> event .-target)]
                (if (not (= 200 (.getStatus response)))
                  (.log js/console (str "Failed to post user profile!  Server response: " (.getResponseText response))))) )
            "POST" (JSON.stringify (clj->js user-profile))
            (clj->js {"content-type" "application/json"})
            ) )


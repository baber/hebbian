(ns com.infimany.hebbian.app.services
  (:require
            [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan close! <! >!]]
            )
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(def user-port "3001")
(def event-port "3000")


; user services.
(defn get-user [chan]
  (xhr/send (str "http://localhost:" user-port "/user/1")
            (fn [event]
              (let [res (js->clj (-> event .-target .getResponseJson) :keywordize-keys true)]
                (.log js/console (pr-str "Getting user in service: "  chan) ) (go (>! chan res))))))


(defn save-user-profile [user-profile]
  (xhr/send (str "http://localhost:" user-port "/user")
            (fn [event]
              (let [response (-> event .-target)]
                (if (not (= 200 (.getStatus response)))
                  (.log js/console (str "Failed to post user profile!  Server response: " (.getResponseText response))))) )
            "POST" (JSON.stringify (clj->js user-profile))
            (clj->js {"content-type" "application/json"})
            ) )


; event services.

(defn get-events [chan criteria]
  (.log js/console "Getting events: " criteria)
  (xhr/send (str "http://localhost:" event-port "/event?postcode=" (:postcode criteria) "&distance=" (:distance criteria))
            (fn [event]
              (let [res (js->clj (-> event .-target .getResponseJson) :keywordize-keys true)]
                (go (>! chan res))))))


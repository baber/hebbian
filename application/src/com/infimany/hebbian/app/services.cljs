(ns com.infimany.hebbian.app.services
  (:require
            [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan close! <! >!]]
            )
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))


(def timeline-url "http://localhost:6060/666")


; event services.

(defn get-events [chan account-id]
  (xhr/send timeline-url
            (fn [event]
              (let [res  (js->clj (-> event .-target .getResponseJson) :keywordize-keys true) ]
                (go (>! chan res)))) _ _ (clj->js {:X-User-Agent "agent"})_)
  )


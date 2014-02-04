(ns app-pedestal.rendering
  (:require
   [io.pedestal.app.render.push :as render]
   [io.pedestal.app.render.push.templates :as templates]
   [io.pedestal.app.render.push.handlers.automatic :as d]
   [io.pedestal.app.render.events :as events]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [domina :as dom]
   [dommy.core :as dommy]
   [io.pedestal.app.messages :as msg]
   )

  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel1 deftemplate]]))


(deftemplate create-user-node [user-details]
  [:table
   [:tr [:td "Identity-id:"] [:td (:identity-id user-details)]]
   [:tr [:td "First Name:"] [:td (:first-name user-details)]]
   [:tr [:td "Last Name:"] [:td (:last-name user-details)]]
   [:tr [:td "Email:"] [:td (:email user-details)]]]
  )


(defn render-user-details [renderer [_ path old-value new-value] _]
  (dommy/clear! (sel1 :#user-details))
  (dommy/append! (sel1 :#user-details) (create-user-node new-value))
)


(defn button-enable [r [_ path transform-name messages] d]
  (.log js/console (pr-str (sel1 "msg-button")))
  (events/send-on-click (sel1 :#msg-button)
                          d
                          transform-name
                          [{msg/type :user msg/topic [:new-details] :value {:identity-id 654321 :email "newbob@bobbo.com" :first-name "New" :last-name "Bob"}}]))

(defn render-config []
  [[:value [:**] render-user-details]
   [:transform-enable [:main :user-details] button-enable]])

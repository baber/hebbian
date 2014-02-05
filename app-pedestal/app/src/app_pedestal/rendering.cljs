(ns app-pedestal.rendering
  (:require
   [io.pedestal.app.render.push.templates :as templates]
   [io.pedestal.app.render.events :as events]
   [dommy.core :as dommy]
   [io.pedestal.app.messages :as msg]
   )

  (:require-macros
   [dommy.macros :refer [node sel1 deftemplate]])
)


(deftemplate create-user-node [user-details]
  [:form {:id "user-details-form"}
   "First name" [:input {:id "first-name" :type "text" :name "first-name" :value (:first-name user-details)}]
   "Last name" [:input {:type "text" :name "last-name" :value (:last-name user-details)}]
   "Email" [:input {:type "email" :name "email" :value (:email user-details)}]
   ]
  )

(defn collect-user-form []
  {:first-name (dommy/value (sel1 :#first-name))}
)


(defn render-user-details [renderer [_ path old-value new-value] _]
  (dommy/clear! (sel1 :#user-details))
  (dommy/append! (sel1 :#user-details) (create-user-node new-value))
)


(defn button-enable [r [_ path transform-name messages] d]
  (events/send-on-click (sel1 :#edit-profile-btn)
                          d
                          transform-name
                          [{msg/type :user msg/topic [:active-user] :value "123456"}]))

(defn button-enable [r [_ path transform-name messages] d]
  (events/send-on-click (sel1 :#edit-profile-btn)
                          d
                          transform-name
                          [{msg/type :user msg/topic [:active-user] :value "123456"}]))


(defn render-config []
  [[:value [:main :user-details] render-user-details]
   [:transform-enable [:main :user-details] button-enable]])

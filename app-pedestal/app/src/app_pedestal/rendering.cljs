(ns app-pedestal.rendering
  (:require
   [io.pedestal.app.render.push.templates :as templates]
   [io.pedestal.app.render.events :as events]
   [dommy.core :as dommy]
   [io.pedestal.app.messages :as msg]
   [app-pedestal.services :as services]
   [io.pedestal.app.protocols :as p]
   )

  (:require-macros
   [dommy.macros :refer [node sel1 deftemplate]])
)


(deftemplate create-user-node [user-details]
  [:form {:id "user-details-form"}
   "Identity Id" [:input {:id "identity-id" :type "text" :name "identity-id" :value (:identity-id user-details)}]
   "First name" [:input {:id "first-name" :type "text" :name "first-name" :value (:first-name user-details)}]
   "Last name" [:input {:id "last-name" :type "text" :name "last-name" :value (:last-name user-details)}]
   "Email" [:input {:id "email" :type "email" :name "email" :value (:email user-details)}]
   ]
  )

(defn collect-user-form []
  {:identity-id (dommy/value (sel1 :#identity-id))
   :first-name (dommy/value (sel1 :#first-name))
   :last-name (dommy/value (sel1 :#last-name))
   :email (dommy/value (sel1 :#email))
   }
  )


(defn render-user-details [renderer [_ path old-value new-value] _]
  (dommy/clear! (sel1 :#user-details))
  (dommy/append! (sel1 :#user-details) (create-user-node new-value))
)


(defn wire-edit-profile-btn [input-queue]
  (dommy/listen! (sel1 :#edit-profile-btn)
                 :click (fn [event] (services/get-user input-queue)) ))

(defn wire-update-profile-btn [input-queue]
  (dommy/listen! (sel1 :#update-profile-btn)
                 :click #(p/put-message input-queue
                               {msg/type :update msg/topic [:user-profile] :value (collect-user-form)}
                               ) ))

(defn wire-buttons [renderer [_ path transform-name messages] input-queue]
  (wire-edit-profile-btn input-queue )
  (wire-update-profile-btn input-queue))

(defn render-config []
  [[:value [:main :user-profile] render-user-details]
   [:transform-enable [:main :user-profile] wire-buttons]
])

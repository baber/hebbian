(ns app-pedestal.rendering
  (:require
   [io.pedestal.app.render.push.templates :as templates]
   [io.pedestal.app.render.events :as events]
   [dommy.core :as dommy]
   [io.pedestal.app.messages :as msg]
   [app-pedestal.services :as services]
   [io.pedestal.app.protocols :as p]
   )

  (:use
   [React.DOM :only [form input label div fieldset]]
   )

  (:require-macros
   [dommy.macros :refer [node sel1 deftemplate]])
)



; React components

(def UserInputField
  (js/React.createClass
   #js {
        :getInitialState (fn [] #js {})

        :render
        (fn []
          (this-as this
                   (fieldset nil
                             (div #js {:className "pure-control-group"}
                                  (label #js {:forName (.. this -props -id)} (.. this -props -name))
                                  (input #js {:id (.. this -props -id) :type "text" :value (.. this -state -value) :onChange (.. this -handleChange)}))
                             ))

          )

        :handleChange
        (fn [event] (this-as this (.setState this #js {:value (.. event -target -value)})))

        :componentWillReceiveProps
        (fn [new-props] (this-as this (.setState this #js {:value (:initialText (js->clj new-props :keywordize-keys true))} nil) ))

        }
   )
  )



(def UserProfile
  (js/React.createClass
   #js {
        :getInitialState (fn [] (this-as this #js{:user (.. this -props -user)}))
        :render
        (fn []
          (this-as this (let [user-details (js->clj (.. this -state -user))]
                          (form #js {:className "pure-form pure-form-aligned"}
                                (UserInputField #js {:id "identity-id" :name "Identity Id" :initialText (:identity-id user-details)})
                                (UserInputField #js {:id "first-name" :name "First Name" :initialText (:first-name user-details)})
                                (UserInputField #js {:id "last-name" :name "Last Name" :initialText (:last-name user-details)})
                                (UserInputField #js {:id "email" :name "Email" :initialText (:email user-details)})
                                )
                          )))
        })
  )



; end React components

(defn render-user-details [renderer [_ path old-value new-value] _]
  (.setState user-profile #js {:user new-value} nil)
)


(defn collect-user-form []
  {:identity-id (dommy/value (sel1 :#identity-id))
   :first-name (dommy/value (sel1 :#first-name))
   :last-name (dommy/value (sel1 :#last-name))
   :email (dommy/value (sel1 :#email))
   }
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



; attach user profile to DOM here.

(def user-profile (UserProfile #js {:user user-data}))


(js/React.renderComponent
 user-profile
 (.getElementById js/document "user-details"))

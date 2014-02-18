(ns com.infimany.hebbian.app.user-rendering
  (:require
   [dommy.core :as dommy]
   [com.infimany.hebbian.app.services :as services]
   [cljs.core.async :as async]
   )

  (:use
   [React.DOM :only [form input label div fieldset]]
   )


  (:require-macros
   [dommy.macros :refer [node sel1 deftemplate]]
   [cljs.core.async.macros :refer [go]])

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
        :getInitialState (fn [] #js {})
        :render
        (fn []
          (this-as this (let [user-details (js->clj (.. this -state -user) :keywordize-keys true)]
                          (.log js/console (pr-str "In render REACT component: " user-details) )
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

(defn render-user-details [user-profile user-data]
  (.log js/console (pr-str "Rendering user data: " user-data) )
  (.setState user-profile #js {:user (clj->js user-data)} nil)
)


(defn collect-user-form []
  {:identity-id (dommy/value (sel1 :#identity-id))
   :first-name (dommy/value (sel1 :#first-name))
   :last-name (dommy/value (sel1 :#last-name))
   :email (dommy/value (sel1 :#email))
   }
  )



(defn wire-edit-profile-btn [chan]
  (dommy/listen! (sel1 :#edit-profile-btn)
                 :click (fn [event]
                           (services/get-user chan)
                          ) ))

(defn wire-update-profile-btn []
  (dommy/listen! (sel1 :#update-profile-btn)
                 :click #(services/save-user-profile (collect-user-form) )) )


; attach user profile to DOM here.

(def user-profile (UserProfile))



; wire up buttons.

(def user-channel (async/chan))

(wire-edit-profile-btn user-channel)
(wire-update-profile-btn)

(js/React.renderComponent
 user-profile
 (.getElementById js/document "user-details"))


; kick off event loop.
(go (while true (render-user-details user-profile (async/<! user-channel))) )




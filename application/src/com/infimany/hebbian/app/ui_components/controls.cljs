(ns com.infimany.hebbian.app.ui-components.controls
  (:require
   [com.infimany.hebbian.app.services :as services]
   [cljs.core.async :as async]
   [dommy.core :as dommy]
   )

  (:use
   [React.DOM :only [form input label fieldset div button]]
   )

  (:require-macros
   [dommy.macros :refer [sel1]]
   )
  )

; channels
(def events-channel (async/chan))


(def InputField
  (js/React.createClass
   #js {
        :getInitialState (fn [] #js {})

        :render
        (fn []
          (this-as this
                   (div #js {}
                        (label #js {:className "label" :forName (.. this -props -id)} (.. this -props -name))
                        (input #js {:className "input" :id (.. this -props -id) :type "text"  :value (.. this -state -value) :onChange (.. this -handleChange)}))
                             )

          )

        :handleChange
        (fn [event] (this-as this (.setState this #js {:value (.. event -target -value)})))

        }
   )
  )

(defn collect-search-criteria []
  {:postcode (dommy/value (sel1 :#postcode))
   :distance (dommy/value (sel1 :#distance))
   }
  )

(def UpdateButton
  (js/React.createClass
   #js {

        :render
        (fn []
          (this-as this (button #js {:className "button" :onClick (.. this -handleClick)} "Update"))
          )

        :handleClick
        (fn [event]
          (.log js/console "HandleClick called!")
          (services/get-events events-channel (collect-search-criteria))
          )

        }
   )
  )

(def ControlPanel
  (js/React.createClass
   #js {
        :render
        (fn []
          (this-as this (div #js {}
                             (InputField #js {:id "postcode" :name "Postcode:"})
                             (InputField #js {:id "distance" :name "Distance (km):"})
                             (UpdateButton #js {})
                             ))
          )

        })
  )


(js/React.renderComponent
 (ControlPanel)
 (.getElementById js/document "controls"))







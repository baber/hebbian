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
   [cljs.core.async.macros :refer [go]]
   )
  )

; channels
(def events-channel (async/chan))
(def criteria-channel (async/chan))

;state
(def origin (atom nil))



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
   :start-time (dommy/value (sel1 :#start-time))
   :end-time (dommy/value (sel1 :#end-time))
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
          (let [criteria (collect-search-criteria)]
            (services/add-geolocation criteria-channel criteria))
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
                             (InputField #js {:id "start-time" :name "Start Time:"})
                             (InputField #js {:id "end-time" :name "End Time:"})
                             (UpdateButton #js {})
                             ))
          )

        })
  )


(js/React.renderComponent
 (ControlPanel)
 (.getElementById js/document "controls"))

; kick off event loop
(go (while true
      (let [criteria (async/<! criteria-channel)]
        (swap! origin #(:geolocation %2) criteria)
        (services/get-events events-channel criteria)
        )
      ))

(ns com.infimany.hebbian.app.ui-components.controls
  (:require
   [com.infimany.hebbian.app.services :as services]
   [cljs.core.async :as async]
   [dommy.core :as dommy]
   [cljsjs.react :as react]
   )


  (:require-macros
    ;[dommy.macros :refer [sel1]]
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
                   (js/React.DOM.div #js {}
                        (js/React.DOM.label #js {:className "label" :forName (.. this -props -id)} (.. this -props -name))
                        (js/React.DOM.input #js {:className "input" :id (.. this -props -id) :type "text"  :value (.. this -state -value) :onChange (.. this -handleChange)}))
                             )

          )

        :handleChange
        (fn [event] (this-as this (.setState this #js {:value (.. event -target -value)})))

        }
   )
  )

(def InputFieldFactory (js/React.createFactory InputField))

(defn collect-search-criteria []
  {:postcode (dommy/value (dommy/sel1 :#postcode))
   :distance (dommy/value (dommy/sel1 :#distance))
   :start-time (dommy/value (dommy/sel1 :#start-time))
   :end-time (dommy/value (dommy/sel1 :#end-time))
   }
  )


(def UpdateButton
  (js/React.createClass
   #js {

        :render
        (fn []
          (this-as this (js/React.DOM.button #js {:className "button" :onClick (.. this -handleClick)} "Update"))
          )

        :handleClick
        (fn [event]
          (let [criteria (collect-search-criteria)]
            (services/add-geolocation criteria-channel criteria))
          )

        }
   )
  )

(def UpdateButtonFactory (js/React.createFactory UpdateButton))



(def ControlPanel
  (js/React.createClass
   #js {
        :render
        (fn []
          (this-as this (js/React.DOM.div #js {}
                             (InputFieldFactory #js {:id "postcode" :name "Postcode:"})
                             (InputFieldFactory #js {:id "distance" :name "Distance (km):"})
                             (InputFieldFactory #js {:id "start-time" :name "Start Time:"})
                             (InputFieldFactory #js {:id "end-time" :name "End Time:"})
                             (UpdateButtonFactory #js {})
                             ))
          )

        })
  )

(js/React.render
  ((js/React.createFactory ControlPanel))
  (.getElementById js/document "controls"))

 kick off event loop
(go (while true
      (let [criteria (async/<! criteria-channel)]
        (swap! origin #(:geolocation %2) criteria)
        (services/get-events events-channel criteria)
        )
      ))

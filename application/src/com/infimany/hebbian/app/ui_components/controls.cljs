(ns com.infimany.hebbian.app.ui-components.controls

  (:use
   [React.DOM :only [form input label fieldset div]]
   )

  )


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


(def ControlPanel
  (js/React.createClass
   #js {
        :getInitialState (fn [] #js {})
        :render
        (fn []
          (this-as this (let [user-details (js->clj (.. this -state -user) :keywordize-keys true)]
                          (div #js {}
                               (InputField #js {:id "postcode" :name "Postcode:"})
                               (InputField #js {:id "distance" :name "Distance (km):"})
                               )
                          )))
        })
  )




(js/React.renderComponent
 (ControlPanel)
 (.getElementById js/document "controls"))







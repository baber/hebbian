(ns com.infimany.hebbian.app.ui-components.events
  (:require
   [cljs.core.async :as async]
   )

  (:use
   [React.DOM :only [div]]
   )

  (:require-macros
   [cljs.core.async.macros :refer [go]])

  )

(def width 1200)
(def height 800)


(defn get-position-css [{location :screen-location z-plane :z-plane}]
  #js {:position "absolute"
   :-webkit-transform (str "translate3d(" (first location) "px," (last location) "px," z-plane "px)")
   }
  )


(def events-channel (async/chan))
(def pan-channel (async/chan (async/sliding-buffer 100)))
(def zoom-channel (async/chan (async/sliding-buffer 100)))


(def OriginMarker
  (js/React.createClass
   #js {
        :render
        (fn [] (this-as this
                              (let [marker (js->clj (.. this -props -marker) :keywordize-keys true)]
                                (div #js {:className "tunnel" :style  (get-position-css marker)})
                                )

                              ))
        }
   )
  )


(def Event
  (js/React.createClass
   #js {

        :render
        (fn []
          (this-as this
                   (let [event (js->clj (.. this -props -event) :keywordize-keys true)]
                     (div #js {:className "event" :style  (get-position-css event)}
                          (div #js {:className "distance"} (:distance event) )
                          (div #js {:className "details"} (:details event))
                          (let [start-time (:start-time event) end-time (:end-time event)]
                            (div #js {:className "time"} (.format (:start-time event) "DD MMM YYYY")))
                          )))

          )

        }
   )
  )

(def EventUniverse
  (js/React.createClass
   #js {

        :getInitialState (fn [] (clj->js {:events [] :markers []}))

        :render
        (fn []
          (this-as this (div #js {:style #js {:width (str width "px") :height (str height "px")}
                                  :onWheel (.. this -handleMouseWheel)
                                  }
                             (into-array (map #(OriginMarker #js {:marker %}) (.. this -state -markers)) )
                             (into-array (map #(Event #js {:event %}) (.. this -state -events)) )
                             ))
          )

        :handleMouseWheel
        (fn [event]
          (.preventDefault event)
          (let [delta-x (.. event -deltaX) delta-y (.. event -deltaY)]
            (go
             (cond
              (>= (js/Math.abs delta-y) (js/Math.abs delta-x)) (async/>! zoom-channel {:x delta-x :y delta-y})
              :else (async/>! pan-channel {:x delta-x :y delta-y}))
             )
            )
          )

        }
   )

)


(def event-universe (EventUniverse))

(js/React.renderComponent
 event-universe
 (.getElementById js/document "events"))


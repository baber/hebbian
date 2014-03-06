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


(def pan-channel (async/chan))
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

(defn fibo []
  (map first (iterate (fn [[a b]] [b (+ a b)]) [0 1]) )
)


(defn generate-pan-sequence [n]
  (take n (fibo))
)



(defn get-pan-deltas [keycode steps]
  (cond
   (= 38 keycode) (map (fn [n] {:x 0 :y (* -1 n)}) (generate-pan-sequence steps))
   (= 40 keycode) (map (fn [n] {:x 0 :y n}) (generate-pan-sequence steps))
   (= 37 keycode) (map (fn [n] {:x n :y 0}) (generate-pan-sequence steps))
   (= 39 keycode) (map (fn [n] {:x (* -1 n) :y 0}) (generate-pan-sequence steps))
   :else nil
   )
  )



(def EventUniverse
  (js/React.createClass
   #js {

        :getInitialState (fn [] (clj->js {:events [] :markers []}))

        :render
        (fn []
          (this-as this (div #js {:style #js {:width "100%" :height "100%"}
                                  :tabIndex "1"
                                  :onWheel (.. this -handleMouseWheel)
                                  :onKeyDown (.. this -handleKeyPress)
                                  }
                             (into-array (map #(OriginMarker #js {:marker %}) (.. this -state -markers)) )
                             (into-array (map #(Event #js {:event %}) (.. this -state -events)) )
                             ))
          )

        :handleMouseWheel
        (fn [event]
          (.preventDefault event)
          (.log js/console (.. event -deltaX))
          (let [delta-x (.. event -deltaX) delta-y (.. event -deltaY)]
            (go
             (cond
              (>= (js/Math.abs delta-y) (js/Math.abs delta-x)) (async/>! zoom-channel {:x delta-x :y delta-y})
              :else (async/>! pan-channel {:x delta-x :y 0}))
             )
            )
          )


        :handleKeyPress
        (fn [event]
          (let [keyCode (.-keyCode event)]
             (let [pan-deltas (get-pan-deltas keyCode 12)]
               (if pan-deltas
                 (dorun (for [delta pan-deltas] (go (.log js/console (str "in callback " delta)) (async/>! pan-channel delta)) ))
                 )
               ))
          )
        }
   )

)


(def event-universe (EventUniverse))

(js/React.renderComponent
 event-universe
 (.getElementById js/document "events"))


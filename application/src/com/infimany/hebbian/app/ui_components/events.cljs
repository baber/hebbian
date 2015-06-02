(ns com.infimany.hebbian.app.ui-components.events
  (:require
   [cljs.core.async :as async]
   [cljsjs.react :as react]
   )

  ;(:use
  ; [cljsjs.react :only [div]]
  ; )

  (:require-macros
   [cljs.core.async.macros :refer [go]])

  )

; ui interaction state
(def x-translation (atom 0))
(def y-translation (atom 0))
(def z-translation (atom 0))
(def rotation (atom 0))
(def visited-events (atom #{}))
(def event-rotations (atom {}))


; channels
(def rotations-channel (async/chan (async/sliding-buffer 1000)))


; css functions.
(defn get-translation-css [{location :screen-location z-plane :z-plane :as event} rotation-angle offsets]
  #js {:position "absolute"
       :-webkit-transform (str "translate3d(" (+ (:x offsets) (first location)) "px,"
                               (+ (:y offsets) (last location)) "px,"
                               (+ (:z offsets) z-plane) "px) " (str "rotateY(" rotation-angle "deg)"))
       :-webkit-backface-visibility "hidden"
       }
  )




; animation functions

(defn update-ui [] (.forceUpdate event-universe))

(defn rotate-events [angles]
  (swap! rotation #(first angles))
  (update-ui)
  (js/setTimeout rotate-events 10 (rest angles))
)


(defn get-rotation-angle [{status :status id :_id}]
  (cond
   (and (= status "new") (not (contains? @visited-events id))) @rotation
   :else ((keyword id) @event-rotations 0)
   )
  )


(defn fibo []
  (map first (iterate (fn [[a b]] [b (+ a b)]) [10 11]) )
)

(defn generate-pan-sequence [n]
  (let [pan-sequence (take n (fibo))]
    (flatten (map #(repeat 2 %) (concat pan-sequence (reverse pan-sequence))))
    )
  )

(def keycode-functions
  {
   :37  (fn [n] {:x n :y 0})
   :38  (fn [n] {:x 0 :y (* -1 n)})
   :39  (fn [n] {:x (* -1 n) :y 0})
   :40  (fn [n] {:x 0 :y n})
   })



(defn get-pan-deltas [keycode steps]
    (map ((keyword (str keycode)) keycode-functions) (generate-pan-sequence steps))
  )


(defn dispatch-deltas [deltas]
  (let [delta (first deltas) x (:x delta)]
    (if (> (js/Math.abs x) 0) (swap! x-translation - x)
      (swap! y-translation + (:y delta))
      )
    (update-ui)
    )
  (if (> (count deltas) 1)
    (js/setTimeout dispatch-deltas 30 (rest deltas)))
  )


; react components
(def OriginMarker
  (js/React.createClass
   #js {
        :render
        (fn [] (this-as this
                              (let [marker (js->clj (.. this -props -marker) :keywordize-keys true) offsets (js->clj (.. this -props -offsets) :keywordize-keys true)]
                                (js/React.DOM.div #js {:className "tunnel" :style  (get-translation-css marker 0 offsets)})
                                )

                              ))
        }
   )
  )

(def OriginMarkerFactory (js/React.createFactory OriginMarker))


(def Event
  (js/React.createClass
   #js {

        :render
        (fn []
          (this-as this
                   (let [event (js->clj (.. this -props -event) :keywordize-keys true)
                         offsets (js->clj (.. this -props -offsets) :keywordize-keys true)
                         rotation-angle (get-rotation-angle event)]
                     (js/React.DOM.div #js {:style {}}
                          (js/React.DOM.div #js {:className "event" :style  (get-translation-css event rotation-angle offsets)
                                    :onClick  (partial (.. this -doRotate) 0 185 5)
                                    :onMouseEnter (.. this -handleMouseEnter)
                                    }
                               (js/React.DOM.div #js {:className "distance"} (str (:distance event) "km") )
                               (js/React.DOM.div #js {:className "details"} (:details event))
                               (let [start-time (:start-time event) end-time (:end-time event)]
                                 (js/React.DOM.div #js {:className "time"} (.format (:start-time event) "DD MMM YYYY")))
                               )

                          (js/React.DOM.div #js {:className "event" :style (get-translation-css event (+ 180 rotation-angle) offsets)
                                    :onClick  (partial (.. this -doRotate) 180 365 5)
                                    :onMouseEnter (.. this -handleMouseEnter)} "Lorum Ipsum!" ))

                     ))

          )

        :doRotate
        (fn [start stop step _]  (this-as this (let [event-id (:_id (js->clj (.. this -props -event) :keywordize-keys true))]
                                 (dorun (for [angle (range start stop step)]
                                          (go (async/>! rotations-channel {:id event-id :angle angle})) ))) ))


        :handleMouseEnter
        (fn [_]  (this-as this (let [event-id (:_id (js->clj (.. this -props -event) :keywordize-keys true))]
                                 (if (not (contains? @visited-events event-id)) (swap! visited-events conj event-id) )
                                 ) ))

        }
   )
  )

(def EventFactory (js/React.createFactory Event))


(def EventUniverse
  (js/React.createClass
   #js {

        :getInitialState (fn [] (clj->js {:events [] :markers []}))

        :render
        (fn []
          (this-as this (js/React.DOM.div #js {:style #js {:width "100%" :height "100%"}
                                  :tabIndex "1"
                                  :onWheel (.. this -handleMouseWheel)
                                  :onKeyDown (.. this -handleKeyDown)
                                  }
                             (let [offsets {:x @x-translation :y @y-translation :z @z-translation}]
                               (into-array (concat (map #(OriginMarkerFactory #js {:marker % :offsets offsets}) (.. this -state -markers))
                                                   (map #(EventFactory #js {:event % :offsets offsets}) (.. this -state -events) ) ))
                               ))
          ) )

        :handleMouseWheel
        (fn [event]
          (.preventDefault event)
          (let [delta-x (.. event -deltaX) delta-y (.. event -deltaY)]
            (go
             (cond
              (>= (js/Math.abs delta-y) (js/Math.abs delta-x)) (do (swap! z-translation + delta-y)  (update-ui) )
              :else  (do (swap! x-translation - delta-x)  (update-ui) )
              )
             )
            )
          )


        :handleKeyDown
        (fn [event]
          (.preventDefault event)
          (let [keyCode (.-keyCode event)]
             (let [deltas (get-pan-deltas keyCode 3)]
               (if deltas
                 (dispatch-deltas deltas)
                  )
               ))
          )
        }
   )

)


(def event-universe (js/React.render
                      ((js/React.createFactory EventUniverse))
                      (.getElementById js/document "events")))

;(js/React.render
;  ((js/React.createFactory EventUniverse))
;  (.getElementById js/document "events"))

; kick off automated rotation animation for pushed events.
(rotate-events (cycle (range 0 360 1)))

; kick off event loop for processing event card flips via user interaction.
(go (while true
      (async/<! (async/timeout 10))
      (let [rotation (async/<! rotations-channel)]
        (swap! event-rotations #(assoc %1 (keyword (:id rotation)) (:angle rotation)))
        (update-ui)
        )))


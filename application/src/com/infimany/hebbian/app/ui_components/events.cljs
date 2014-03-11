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

; state
(def x-translation (atom 0))
(def y-translation (atom 0))
(def z-translation (atom 0))
(def rotation (atom 0))
(def visited-events (atom #{}))

(def angles (cycle (range 360)))


(defn get-animation-transforms [event]
  (if (and (= (:status event) "new") (not (contains? @visited-events (:_id event)))) (str "rotateY(" @rotation "deg)") "")
)

(defn get-position-css [{location :screen-location z-plane :z-plane :as event} offsets]
  #js {:position "absolute"
       :-webkit-transform (str "translate3d(" (+ (:x offsets) (first location)) "px,"
                               (+ (:y offsets) (last location)) "px,"
                               (+ (:z offsets) z-plane) "px) " (get-animation-transforms event))
       }
  )




; utility functions


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
                                (div #js {:className "tunnel" :style  (get-position-css marker offsets)})
                                )

                              ))
        }
   )
  )



(def Event
  (js/React.createClass
   #js {

;        :getInitialState (fn [] (clj->js {:visited false}))

        :render
        (fn []
          (this-as this
                   (let [event (js->clj (.. this -props -event) :keywordize-keys true) offsets (js->clj (.. this -props -offsets) :keywordize-keys true)]
                     (div #js {:className "event" :style  (get-position-css event offsets) :onMouseEnter  (.. this -handleMouseOver) }
                          (div #js {:className "distance"} (:distance event) )
                          (div #js {:className "details"} (:details event))
                          (let [start-time (:start-time event) end-time (:end-time event)]
                            (div #js {:className "time"} (.format (:start-time event) "DD MMM YYYY")))
                          )))

          )

        :handleMouseOver
        (fn [_] (this-as this (let [event (js->clj (.. this -props -event) :keywordize-keys true)]
                                (if (not (contains? @visited-events (:_id event))) (swap! visited-events conj (:_id event)) )  )))

        }
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
                                  :onKeyDown (.. this -handleKeyDown)
                                  }
                             (let [offsets {:x @x-translation :y @y-translation :z @z-translation}]
                               (into-array (map #(OriginMarker #js {:marker % :offsets offsets}) (.. this -state -markers)) )
                               (into-array (map #(Event #js {:event % :offsets offsets}) (.. this -state -events)) )
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
             (let [deltas (get-pan-deltas keyCode 5)]
               (if deltas
                 (dispatch-deltas deltas)
                  )
               ))
          )
        }
   )

)

(def event-universe (EventUniverse))


(defn update-ui [] (.forceUpdate event-universe))

(defn rotate-events [angles]
  (swap! rotation #(first angles))
  (update-ui)
  (js/setTimeout rotate-events 10 (rest angles))
)

(js/React.renderComponent
 event-universe
 (.getElementById js/document "events"))


(rotate-events angles)



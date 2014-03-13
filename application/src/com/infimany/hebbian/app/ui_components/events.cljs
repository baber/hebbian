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




; non-css animation functions


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
                                (div #js {:className "tunnel" :style  (get-translation-css marker 0 offsets)})
                                )

                              ))
        }
   )
  )


;; (defn rotate-event [id direction angles]
;;   (let [animation ((keyword id) @event-animations)]
;;     (if (and (= direction (:direction animation)) (> (count angles) 0))
;;       (do (swap! event-animations #(assoc %1 (keyword id) {:direction direction :angle %2}) (first angles) )
;;         (update-ui)
;;         (js/setTimeout rotate-event 10 (rest angles))
;;         )
;;       )
;;     )
;;   )

(defn get-rotation-angle [{status :status id :_id}]
  (cond
   (and (= status "new") (not (contains? @visited-events id))) @rotation
   :else ((keyword id) @event-rotations 0)
   )
  )



(def Event
  (js/React.createClass
   #js {

        :render
        (fn []
          (this-as this
                   (let [event (js->clj (.. this -props -event) :keywordize-keys true)
                         offsets (js->clj (.. this -props -offsets) :keywordize-keys true)
                         rotation-angle (get-rotation-angle event)]
                     (div #js {:style {}}
                          (div #js {:className "event" :style  (get-translation-css event rotation-angle offsets)
                                    :onClick  (.. this -showBack)
                                    ;                                    :onMouseLeave (.. this -handleMouseLeave)
                                    }
                               (div #js {:className "distance"} (:distance event) )
                               (div #js {:className "details"} (:details event))
                               (let [start-time (:start-time event) end-time (:end-time event)]
                                 (div #js {:className "time"} (.format (:start-time event) "DD MMM YYYY")))
                               )

                          (div #js {:className "event" :style (get-translation-css event (+ 180 rotation-angle) offsets) :onClick  (.. this -showFront)} "Lorum Ipsum!" ))

                     ))

          )

        :showBack
        (fn [_]  (this-as this (let [event-id (:_id (js->clj (.. this -props -event) :keywordize-keys true))]
                                 (dorun (for [angle (range 0 180 1)]
                                          (go (async/>! rotations-channel {:id event-id :angle angle})) ))) ))


        :showFront
        (fn [_]  (this-as this (let [event-id (:_id (js->clj (.. this -props -event) :keywordize-keys true))]
                                 (dorun (for [angle (range 180 360 1)]
                                          (go (async/>! rotations-channel {:id event-id :angle angle})) ))) ))
        ;;        (fn [_]
        ;;           (this-as this (let [event (js->clj (.. this -props -event) :keywordize-keys true)
        ;;                               id (:_id event)
        ;;                               animation ((keyword id) @event-animations)
        ;;                               current-angle (:angle animation 180)]
        ;;                           (swap! event-animations #(assoc %1 (keyword id) {:direction :clockwise :angle current-angle}) )
        ;;                           (rotate-event id :clockwise (range current-angle 0 -1))
        ;;                           ))
        ;;           )

        :handleMouseEnter
        (fn [_]  (this-as this (let [event-id (:_id (js->clj (.. this -props -event) :keywordize-keys true))]
                                 (dorun (for [angle (range 0 180 1)]
                                          (go (async/>! rotations-channel {:id event-id :angle angle})) ))) ))

        ;;         (fn [_]
        ;;           (this-as this (let [event (js->clj (.. this -props -event) :keywordize-keys true)
        ;;                               id (:_id event)
        ;;                               animation ((keyword id) @event-animations)
        ;;                               current-angle (:angle animation 0)]
        ;;                           (swap! event-animations #(assoc %1 (keyword id) {:direction :anticlockwise :angle current-angle}) )
        ;;                           (rotate-event id :anticlockwise (range current-angle 0 -1))
        ;;                           ))
        ;;           )


        ;;         (fn [_] (this-as this (let [event (js->clj (.. this -props -event) :keywordize-keys true)]
        ;;                                 (if (not (contains? @visited-events (:_id event))) (swap! visited-events conj (:_id event)) )  )))

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
                               (into-array (concat (map #(OriginMarker #js {:marker % :offsets offsets}) (.. this -state -markers))
                                                   (map #(Event #js {:event % :offsets offsets}) (.. this -state -events) ) ))
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

; kick off rotation animation for pushed events.
;(rotate-events (cycle (range 0 360 1)))

; kick off event loop for processing event card rotations.
(def interval 10)


(go (while true
      (async/<! (async/timeout interval))
      (let [rotation (async/<! rotations-channel)]
        (swap! event-rotations #(assoc %1 (keyword (:id rotation)) (:angle rotation)))
        (update-ui)
        )))


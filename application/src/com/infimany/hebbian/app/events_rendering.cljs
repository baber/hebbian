(ns com.infimany.hebbian.app.events-rendering
  (:require
   [dommy.core :as dommy]
   [cljs.core.async :as async]
   )

  (:use
   [React.DOM :only [div]]
   )

  (:require-macros
   [cljs.core.async.macros :refer [go]])

)

(def events (atom [
              {:location [150 500] :details "Cricket Club Annual Dinner" :date "21/12/13" :time "19:30 to 21:30" :z-plane -100}
              {:location [10 100], :details "OAP Party", :date "03/04/14" :time "15:00" :z-plane 0}
              {:location [800 200], :details "Paper Mill Viewing", :date "13/03/14" :time "11:00" :z-plane 200}
              {:location [450 350], :details "Nash Mills School Fete", :date "21/06/14" :time "11:00 to 18:00" :z-plane -500}
             ]))



; positioning functions.

(def max-x 1100)
(def max-y 700)



(defn get-distance [geohash origin]
  3 )

(defn in-bounds? [location]
  true
;;   (let [x (first location) y (last location)]
;;     (and
;;      (> x 0)
;;      (> y 0)
;;      (< x max-x)
;;      (< y max-y)
;;      ))
  )

(defn to-renderable [event]
  (let [geolocation (:location event)]
    (merge event {:distance (get-distance geolocation nil)}))
  )

(defn get-position-css [renderable-event]
  (let [location (:location renderable-event)]
    {
     :position "absolute"
     :left (str (first location) "px")
     :top (str (last location) "px")
     }
    )
)

(defn get-dimension-css [renderable-event]
  {:width (str (:width renderable-event) "px") :height (str (:height renderable-event) "px")}
)

(defn get-z-plan-css [renderable-event]
  {:-webkit-transform (str "translateZ(" (:z-plane renderable-event) "px)")}
)

(defn get-location-css [renderable-event]
  (clj->js (merge (get-position-css renderable-event)
                  (get-dimension-css renderable-event)
                  (get-z-plan-css renderable-event)
                  (cond
                   (in-bounds? (:location renderable-event)) {}
                   :else {:display "none"}) ))
)

; channels.

(def pan-channel (async/chan (async/sliding-buffer 100)))
(def zoom-channel (async/chan (async/sliding-buffer 100)))


; React components.


(def Event
  (js/React.createClass
   #js {

        :render
        (fn []
          (this-as this
                   (let [event-data (.. this -props -event)]
                     (div #js {:className "event" :style  (get-location-css (js->clj event-data))}
                          (div #js {:className "distance"} (:distance event-data) )
                          (div #js {:className "details"} (:details event-data))
                          (div #js {:className "time"} (str (:date event-data) " - " (:time event-data)))
                          )))

          )

        }
   )
  )


(def EventUniverse
  (js/React.createClass
   #js {

        :getInitialState (fn [] (clj->js {:events @events}))

        :render
        (fn []
          (this-as this (div #js {:style #js {:width "1200px" :height "800px" :overflow-x "scroll"}
                                  :onWheel (.. this -handleMouseWheel)
                                  :onMouseOver  (.. this -handleMouseDrag)

                                  }

                             (into-array (map #(Event #js {:event %}) (map #(to-renderable (js->clj % :keywordize-keys true)) (.. this -state -events))))
                             ))
          ),

        :handleMouseWheel
        (fn [event]
          (.preventDefault event)
          (let [delta-x (.. event -deltaX) delta-y (.. event -deltaY)]
            (go
             ;(>! pan-channel {:x delta-x :y delta-y})
             (cond
              (> (js/Math.abs delta-x) (js/Math.abs delta-y)) (>! pan-channel {:x delta-x :y delta-y})
              :else (>! zoom-channel {:x delta-x :y delta-y}))
             )
            )
          )

        :handleMouseDrag (fn [event] (.log js/console "mouse drag") )

        }
   )

)



(def event-universe (EventUniverse #js {:events @events}))

(js/React.renderComponent
 event-universe
 (.getElementById js/document "events"))

; end React components.

(defn shift-location [old-location delta]
  [(+ (:x delta) (first old-location))  (last old-location)]
)

(defn shift-locations [events delta]
  (map #(update-in % [:location] shift-location delta) events)
  )


(defn shift-z-plane [old-z-value delta]
  (+ old-z-value (:y delta) )
)



(defn shift-time-planes [events delta]
  (map #(update-in % [:z-plane] shift-z-plane delta) events)
)

; kick off event loop.
(go (while true (let [delta (async/<! pan-channel)]
                  (swap! events shift-locations delta)
                  (.setState event-universe #js {:events (clj->js @events)})
                  ) ) )

(go (while true (let [delta (async/<! zoom-channel)]
                  (swap! events shift-time-planes delta)
                  (.setState event-universe #js {:events (clj->js @events)})
                 )))








(ns com.infimany.hebbian.app.events-rendering
  (:require
   [dommy.core :as dommy]
   [cljs.core.async :as async]
   )

  (:use
   [React.DOM :only [div]]
   [moment :only [moment]]
   )

  (:require-macros
   [cljs.core.async.macros :refer [go]])

)

(def date-fmt "DD-MM-YYYY HH:mm:ss")

(def raw-events [
              {:geohash "geohash1" :details "Cricket Club Annual Dinner" :start-time (js/moment "31-12-2013 19:30:00" date-fmt) :end-time (js/moment "31-12-2013 22:30:00" date-fmt)}
              {:geohash "geohash2" :details "OAP Party", :start-time (js/moment "17-03-2014 15:30:00" date-fmt)}
              {:geohash "geohash3" :details "Paper Mill Viewing", :start-time (js/moment "06-11-2013 10:30:00" date-fmt)}
              {:geohash "geohash4" :details "Nash Mills School Fete",  :start-time (js/moment "21-06-2014 11:00:00" date-fmt) :end-time (js/moment "21-06-2014 18:30:00" date-fmt)}
             ])


(def origin (atom "geohash-origin"))

; positioning functions.

(defn get-distance [{geohash :geohash}]
  3 )

(defn get-screen-loc [{geohash :geohash}]
  (cond
   (= "geohash1" geohash) [10 400]
   (= "geohash2" geohash) [500 100]
   (= "geohash3" geohash) [50 250]
   (= "geohash4" geohash) [395 500]
   )
  )

(defn add-z-plane [events]
  (let [now (js/moment) hours (* 1000 60 60)]
    (map #(assoc % :z-plane (int (/ (.diff now (:start-time %)) hours)) ) events )
    )
  )



(defn to-renderable [event]
    (merge event {:distance (get-distance event)} {:location (get-screen-loc event)}))

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
                  (get-z-plan-css renderable-event) ) )
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
                   (let [event-data (js->clj (.. this -props -event) :keywordize-keys true)]
                     (div #js {:className "event" :style  (get-location-css event-data :keywordize-keys true)}
                          (div #js {:className "distance"} (:distance event-data) )
                          (div #js {:className "details"} (:details event-data))
                          (let [start-time (:start-time event-data) end-time (:end-time event-data)]
                            (div #js {:className "time"} (.format (:start-time event-data) "DD MMM YYYY")))
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
                                  }

                             (into-array (map #(Event #js {:event %}) (.. this -state -events)) )
                             ))
          )

        :handleMouseWheel
        (fn [event]
          (.preventDefault event)
          (let [delta-x (.. event -deltaX) delta-y (.. event -deltaY)]
            (go
             (cond
              (>= (js/Math.abs delta-y) (js/Math.abs delta-x)) (>! zoom-channel {:x delta-x :y delta-y})
              :else (>! pan-channel {:x delta-x :y delta-y}))
             )
            )
          )

        }
   )

)



(def events (atom (map to-renderable (add-z-plane raw-events)) ) )


(def event-universe (EventUniverse))

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
(go (while true
        (let [[value channel] (alts! [pan-channel zoom-channel])]
          (cond
           (= pan-channel channel) (swap! events shift-locations value)
           (= zoom-channel channel) (swap! events shift-time-planes value)
           ) )
      (.setState event-universe #js {:events (clj->js @events)})
))






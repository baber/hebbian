(ns com.infimany.hebbian.app.events-rendering
  (:require
   [dommy.core :as dommy]
   [cljs.core.async :as async]
   [com.infimany.hebbian.app.services :as services]

   )

  (:use
   [React.DOM :only [div]]
   [moment :only [moment]]
   )

  (:require-macros
   [cljs.core.async.macros :refer [go]])

)

(def date-fmt "YYYY-MM-DDThh:mm:ssZ")
(def origin {:lat 51.734262 :lng -0.455852})
(def scale 10000)
(def width 1200)
(def height 800)
(def screen-origin {:x (/ width 2) :y (/ height 2)})
(def event-x-translation 65)
(def event-y-translation 80)


; positioning functions.

(defn get-distance [{geohash :location}]
  3 )

(defn scale-translate [point]
  [(int (+ (:x screen-origin) (* scale (:x point)))) (+ (:y screen-origin) (* scale (:y point)))]
)

(defn move-event-to-center [point]
  [(- (first point) event-x-translation) (- (last point) event-y-translation )]
)

(defn get-screen-loc [{geolocation :geolocation}]
  (move-event-to-center (scale-translate {:x (- (:lng geolocation) (:lng origin)) :y (- (:lat origin) (:lat geolocation))}))
)

(defn add-z-plane [events]
  (let [now (js/moment) hours (* 1000 60 60)]
    (map #(assoc % :z-plane (int (/ (.diff now (js/moment (:start-time %) date-fmt)) hours)) ) events )
    )
  )



(defn to-renderable [event]
    (merge event {:distance (get-distance event)} {:screen-location (get-screen-loc event)}))

(defn get-position-css [renderable-event]
  (let [location (:screen-location renderable-event)]
    {
     :position "absolute"
     :top (str (last location) "px")
     :left (str (first location) "px")

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
                  (get-z-plan-css renderable-event) ) )
)

(defn convert-times [event]
  (reduce #(update-in event [%] js/moment date-fmt) [:start-time :end-time])
)

; React components.


(def Event
  (js/React.createClass
   #js {

        :render
        (fn []
          (this-as this
                   (let [event-data (js->clj (.. this -props -event) :keywordize-keys true)]
                     (div #js {:className "event" :style  (get-location-css event-data)}
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
          (this-as this (div #js {:style #js {:width (str width "px") :height (str height "px")}
                                  :onWheel (.. this -handleMouseWheel)
                                  }
                             (into-array (for [marker @origin-markers]
                                           (div (clj->js {:className "tunnel" :style
                                                          {:position "absolute"
                                                           :left (str (first (:screen-location marker))"px")
                                                           :top (str (last (:screen-location marker))"px")
                                                           :-webkit-transform (str "translateZ(" (:z-plane marker) "px)")}}))))
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

; channels.

(def events-channel (async/chan))
(def pan-channel (async/chan (async/sliding-buffer 100)))
(def zoom-channel (async/chan (async/sliding-buffer 100)))

(defn generate-origin-markers []
  (for [z-plane (range 0 -2000 -200)] {:z-plane z-plane :screen-location [(:x screen-origin) (:y screen-origin)]})
)

(def events (atom []))
(def origin-markers (atom (generate-origin-markers)) )



;(def events (atom (map to-renderable (add-z-plane raw-events)) ) )


(def event-universe (EventUniverse))

(js/React.renderComponent
 event-universe
 (.getElementById js/document "events"))


(services/get-events events-channel)
(go (let [raw-events (async/<! events-channel)]
      (swap! events #(map to-renderable (add-z-plane (map convert-times %2))) raw-events )
      (.setState event-universe #js {:events (clj->js @events)})
      ))



; end React components.

(defn shift-location [old-location delta]
  [(+ (:x delta) (first old-location))  (last old-location)]
)

(defn shift-locations [events delta]
  (map #(update-in % [:screen-location] shift-location delta) events)
  )


(defn shift-z-plane [old-z-value delta]
  (+ old-z-value (:y delta) )
)


(defn shift-z-planes [events delta]
  (map #(update-in % [:z-plane] shift-z-plane delta) events)
)

; kick off event loop.
(go (while true
        (let [[value channel] (alts! [pan-channel zoom-channel])]
          (cond
           (= pan-channel channel) (do (swap! events shift-locations value) (swap! origin-markers shift-locations value) )
           (= zoom-channel channel) (do (swap! events shift-z-planes value) (swap! origin-markers shift-z-planes value))
           ) )
      (.setState event-universe #js {:events (clj->js @events) :origin-markers (clj->js @origin-markers)})
      ))






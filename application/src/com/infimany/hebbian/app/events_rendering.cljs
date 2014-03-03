(ns com.infimany.hebbian.app.events-rendering
  (:require
   [dommy.core :as dommy]
   [cljs.core.async :as async]
   [com.infimany.hebbian.app.services :as services]
   [com.infimany.hebbian.app.ui-components.events :as event-ui]
   [com.infimany.hebbian.app.geolocation-utils :as geoloc]
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


(defn get-distance [{location :geolocation}]
  (.toFixed (geoloc/distance location origin) 2) )

(defn scale-translate [point]
  [(int (+ (:x screen-origin) (* scale (:x point)))) (int (+ (:y screen-origin) (* scale (:y point))))]
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


(defn convert-times [event]
  (reduce #(update-in event [%] js/moment date-fmt) [:start-time :end-time])
)


(defn generate-origin-markers []
  (for [z-plane (range 0 -2000 -200)] {:z-plane z-plane :screen-location [(:x screen-origin) (:y screen-origin)]})
)

(def events (atom []))
(def markers (atom (generate-origin-markers)) )


(services/get-events event-ui/events-channel)
(go (let [raw-events (async/<! event-ui/events-channel)]
      (swap! events #(map to-renderable (add-z-plane (map convert-times %2))) raw-events )
      (.setState event-ui/event-universe #js {:events (clj->js @events) :markers (clj->js @markers)})
      ))


(defn shift-location [old-location delta]
  [(-  (first old-location) (:x delta))  (last old-location)]
  )

(defn shift-locations [elements delta]
  (map #(update-in % [:screen-location] shift-location delta) elements)
  )


(defn shift-z-plane [old-z-value delta]
  (+ old-z-value (:y delta) )
)


(defn shift-z-planes [elements delta]
  (map #(update-in % [:z-plane] shift-z-plane delta) elements)
)

(defn move-objects [delta f]
  (doseq [objects [events markers]]
    (swap! objects f delta)
    )
  )


; kick off event loop.
(go (while true
        (let [[value channel] (async/alts! [event-ui/pan-channel event-ui/zoom-channel])]
          (cond
           (= event-ui/pan-channel channel) (move-objects value shift-locations)
           (= event-ui/zoom-channel channel) (move-objects value shift-z-planes)
           ) )
      (.setState event-ui/event-universe #js {:events (clj->js @events) :markers (clj->js @markers)})
      ))






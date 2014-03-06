(ns com.infimany.hebbian.app.events-rendering
  (:require
   [dommy.core :as dommy]
   [dommy.attrs :as dommy-attr]
   [cljs.core.async :as async]
   [com.infimany.hebbian.app.services :as services]
   [com.infimany.hebbian.app.ui-components.events :as event-ui]
   [com.infimany.hebbian.app.ui-components.controls :as controls-ui]
   [com.infimany.hebbian.app.geolocation-utils :as geoloc]
   )

  (:use
   [React.DOM :only [div]]
   [moment :only [moment]]
   )

  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [dommy.macros :refer [sel1]])

)


(def date-fmt "YYYY-MM-DDThh:mm:ssZ")
(def scale 10000)
(def width (.-offsetWidth (sel1 :#events)))
(def height (.-offsetHeight (sel1 :#events)))
(def screen-origin {:x (/ width 2) :y (/ height 2)})
(def event-x-translation 65)
(def event-y-translation 80)

; state
(def events (atom []))
(def markers (atom []) )

markers

; positioning functions.


(defn get-distance [{{coords :coordinates} :geolocation}]
  (.toFixed (geoloc/distance {:lng (first coords) :lat (last coords)} @controls-ui/origin) 2) )

(defn scale-translate [point]
  [(int (+ (:x screen-origin) (* scale (:x point)))) (int (+ (:y screen-origin) (* scale (:y point))))]
)

(defn move-event-to-center [point]
  [(- (first point) event-x-translation) (- (last point) event-y-translation )]
)

(defn get-screen-loc [{{coords :coordinates} :geolocation}]
  (move-event-to-center (scale-translate {:x (- (first coords) (:lng @controls-ui/origin)) :y (- (:lat @controls-ui/origin) (last coords))}))
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


(defn generate-origin-markers [_]
  (let [z-planes (map #(:z-plane %) @events) min-z (apply min z-planes) max-z (apply max z-planes)]
    (for [z-plane (range max-z min-z -200)] {:z-plane z-plane :screen-location [(:x screen-origin) (:y screen-origin)]})
    )
)



(defn shift-location [old-location delta]
  [(-  (first old-location) (:x delta))  (+ (last old-location) (:y delta))]
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


(defn reload [new-events]
  (swap! events #(map to-renderable (add-z-plane (map convert-times %2))) new-events )
  (swap! markers generate-origin-markers)
)

; kick off event loop.
(go (while true
        (let [[value channel] (async/alts! [event-ui/pan-channel event-ui/zoom-channel controls-ui/events-channel])]
          (cond
           (= event-ui/pan-channel channel) (do (.log js/console (str "panning: " (:x value) "/" (:y value)) )(move-objects value shift-locations))
           (= event-ui/zoom-channel channel) (move-objects value shift-z-planes)
           (= controls-ui/events-channel channel) (reload value)
           ) )
      (.setState event-ui/event-universe #js {:events (clj->js @events) :markers (clj->js @markers)})
      ))






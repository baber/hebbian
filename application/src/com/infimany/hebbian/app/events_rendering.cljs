(ns com.infimany.hebbian.app.events-rendering
  (:require
   [dommy.core :as dommy]
   )

  (:use
   [React.DOM :only [div]]
   )

)

; positioning functions.

(def max-x 1200)
(def max-y 800)

(def event-locations {"geohash1" [150 100] "geohash2" [200 500]  "geohash3" [100 500]  "geohash4" [1100 600] })

(defn get-location [geohash origin]
  (get event-locations geohash))

(defn get-distance [geohash origin]
  3 )

(defn in-bounds? [location]
  (and
   (< (first location) max-x)
   (< (last location) max-y)
   )
  )

(defn to-renderable [event]
  (.log js/console (pr-str "in to-renderable") )
  (let [geolocation (:location event)]
    (merge event {:render-location (get-location geolocation nil) :distance (get-distance geolocation nil)}))
)

(defn get-position-css [renderable-event]
  (let [location (:render-location renderable-event)]
    {
     :position "absolute"
     :left (str (first location) "px")
     :top (str (last location) "px")
     }
    )
)


(defn get-location-css [renderable-event]
  (clj->js (merge (get-position-css renderable-event)
                  (cond
                   (in-bounds? (:render-location renderable-event)) {}
                   :else {:display "none"}) ))
)


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
        :render
        (fn []
          (this-as this (div nil
                             (into-array (map #(Event #js {:event %}) (map #(to-renderable (js->clj %)) (.. this -props -events))))
                         ))
          )
        }
   )

)

(def events [
              {:location "geohash1" :details "Cricket Club Annual Dinner" :date "21/12/13" :time "19:30 to 21:30"}
              {:location "geohash2", :details "OAP Party", :date "03/04/14" :time "15:00"}
              {:location "geohash3", :details "Paper Mill Viewing", :date "13/03/14" :time "11:00"}
              {:location "geohash4", :details "Nash Mills School Fete", :date "21/06/14" :time "11:00 to 18:00"}
             ])


(js/React.renderComponent
 (EventUniverse #js {:events events})
 (.getElementById js/document "events"))

; end React components.








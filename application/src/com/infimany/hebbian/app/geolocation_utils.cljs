(ns com.infimany.hebbian.app.geolocation-utils

)

(def radius 6371) ; approx earth radius in km.

(defn rad [n]
  (* n (/ js/Math.PI 180))
  )
(defn square [n] (* n n))



(defn distance
  "Returns distance (in km) between two lat/long locations based on Haversine formula.
  See: http://www.movable-type.co.uk/scripts/latlong.html"
  [loc1 loc2]
  (let [dLat (rad (- (:lat loc2) (:lat loc1))) dLng (rad (- (:lng loc2) (:lng loc1)))]

    (let [a  (+ (square (js/Math.sin(/ dLat 2)) )
                (apply * [(square (js/Math.sin(/ dLng 2)))  (js/Math.cos (rad (:lat loc1))) (js/Math.cos (rad (:lat loc2)))]))]
      (* radius (* 2 (js/Math.atan2 (js/Math.sqrt a) (js/Math.sqrt (- 1 a)))))
      )
    )

  )


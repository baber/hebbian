(ns com.infimany.hebbian.event-service.geocode-utils
  (:require
   [cheshire.core :refer [generate-string parse-string]]
   [clojure.java.io :as io]
   [clj-http.client :as clj-http]
   )

  )

(def mapquest-key "Fmjtd|luur210znh,2w=o5-90ys0u")

(def geocode-url "http://www.mapquestapi.com/geocoding/v1/address")
(def reverse-geocode-url "http://www.mapquestapi.com/geocoding/v1/reverse")

(def request-params {:query-params {:key mapquest-key} :content-type :json})

(def request-body {:location {:postalCode "hp38aw" :city "hemel hempstead" :county "hertfordshire" :state "" :country "GB"}})


(defn geocode [{location :location}]
  (let [response (clj-http/post geocode-url (assoc request-params :body (generate-string {:location location})) )
        body (parse-string (:body response) true)]
    (if (not (= 200 (:status response))) (do (println "Non 200 status returned!!!!") nil))
    (cond
     (= 0 (:statuscode (:info body))) (:latLng (first (:locations (first (:results body))))) ; taking first result only - what to do if more than one?
     :else (do (println (str "Problem! Detailed message: " (:messages (:info body)) ) ) nil)
     ) )
  )




(defn reverse-geocode [geolocation]
  (let [response (clj-http/post reverse-geocode-url (assoc request-params :body (generate-string {:location {:latLng geolocation}})) )
        body (parse-string (:body response) true)]
    (if (not (= 200 (:status response))) (do (println "Non 200 status returned!!!!") nil))
    (cond
     (= 0 (:statuscode (:info body)))  (:postalCode (first (:locations (first (:results body)))))
     :else (do (println (str "Problem! Detailed message: " (:messages (:info body)) ) ) nil)
     ) )
)


;(reverse-geocode {:lat 51.731000 :lng -0.465543})

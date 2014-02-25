(ns com.infimany.hebbian.event-service.rest.exceptions
  (:use [slingshot.slingshot :only [try+]])
  (:require [ring.util.response :as ring-utils])
  )

(def http-errors {
                  :invalid_json 400})



(defn wrap-exceptions [handler]
  (fn [request]
      (try+
       (handler request)
       (catch map? exception-map
         (ring-utils/status (ring-utils/response (:message exception-map) ) ((:type exception-map) http-errors)) )
       (catch java.lang.Throwable exception (ring-utils/status (ring-utils/response (str (:message &throw-context)) ) 500))
       )
      ))


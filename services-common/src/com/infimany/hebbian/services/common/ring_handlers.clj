(ns com.infimany.hebbian.services.common.ring-handlers
  (:require
   [ring.util.response :as response])
)



(defn cross-domain-access [handler]
  (fn [request]
    (-> (handler request)
        (response/header "Access-Control-Allow-Origin" "*")
        )) )


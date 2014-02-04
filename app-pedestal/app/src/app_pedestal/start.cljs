(ns app-pedestal.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            [app-pedestal.behavior :as behavior]
            [app-pedestal.rendering :as rendering]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [domina :as dom]
            [dommy.core :as dommy])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel1 deftemplate]]))


(defn fetch-user [app]
  (go (let [response (<! (http/get "http://localhost:3001/user/123456" ))]
        (p/put-message (:input app)
                       {msg/type :user msg/topic [:new-details] :value (:body response)}
                       ))))

;; In this namespace, the application is built and started.

(defn create-app []
  (let [
        app (app/build behavior/example-app)
        render-fn (push-render/renderer "content" (rendering/render-config) render/log-fn)
        ;; services-fn (fn [message input-queue] ...)
        app-model (render/consume-app-model app render-fn)]
    ;; (app/consume-effects app services-fn)
    (app/begin app)
    (fetch-user app)
    ;; Returning the app and app-model from the main function allows
    ;; the tooling to add support for useful features like logging
    ;; and recording.
    {:app app :app-model app-model}))

(defn ^:export main []
  ;; config/config.edn refers to this namespace as a main namespace
  ;; for several aspects. A main namespace must have a no argument
  ;; main function. To tie into tooling, this function should return
  ;; the newly created app.
  (create-app))

(ns cs-test.server.router
  (:require
    [compojure.core :refer [defroutes GET POST]]
    [compojure.route :as route]
    [environ.core :as environ]
    [ring.middleware.cors :as cors]
    [ring.middleware.defaults :as defaults]
    [ring.middleware.reload :as reload]
    [ring.util.response :as response]
    [taoensso.sente :as sente]
    [taoensso.sente.server-adapters.http-kit :as http-kit]
    [taoensso.timbre :as log]))

(declare channel-socket)

(defn start-websocket []
  (log/info "Starting websocket...")
  (defonce channel-socket
    (sente/make-channel-socket!
      http-kit/sente-web-server-adapter
      {})))

(defroutes routes
  (GET "/" req (response/content-type
                 (response/resource-response "public/index.html")
                 "text/html"))
  (GET "/status" req (str "Running: " (pr-str @(:connected-uids channel-socket))))
  (GET "/chsk" req ((:ajax-get-or-ws-handshake-fn channel-socket) req))
  (POST "/chsk" req ((:ajax-post-fn channel-socket) req))
  (route/resources "/")
  (route/not-found "Not found"))

(def handler
  (-> #'routes
    (cond-> (environ/env :dev?) (reload/wrap-reload))
    (defaults/wrap-defaults (assoc-in defaults/site-defaults [:security :anti-forgery] false))
    (cors/wrap-cors :access-control-allow-origin [#".*"]
                    :access-control-allow-methods [:get :put :post :delete]
                    :access-control-allow-credentials ["true"])))

(defmulti event :id)

(defmethod event :default [{:keys [event]}]
  (log/info "Unhandled event: " event))

(defmethod event :cs-test/new-game [{:keys [uid ?data]}]
  (log/info "new-game called! uid" uid "?data" ?data))

(defn start-router []
  (log/info "Starting router...")
  (defonce router
    (sente/start-chsk-router! (:ch-recv channel-socket) event)))

#_(defn broadcast []
  (doseq [uid (:any @(:connected-uids channel-socket))]
    ((:send-fn channel-socket) uid [:snakelake/world @model/world])))

(defn init []
  (start-websocket)
  (start-router))

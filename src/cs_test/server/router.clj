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
    [taoensso.timbre :as log]
    [cs-test.server.model :as model]))

(declare channel-socket)

(defn start-websocket []
  (log/info "Starting websocket...")
  (defonce channel-socket
    (sente/make-channel-socket!
      http-kit/sente-web-server-adapter
      {:user-id-fn (fn [req] (:client-id req))})))

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

#_(defn broadcast []
  (doseq [uid (:any @(:connected-uids channel-socket))]
    ((:send-fn channel-socket) uid [:snakelake/world @model/world])))

(defn broadcast-game-state [players event-and-payload]
  (doseq [player players]
    ((:send-fn channel-socket) (first (keys player)) event-and-payload)))

(defmulti event :id)

(defmethod event :default [{:keys [event]}]
  (log/info "Unhandled event: " event))

(defmethod event :cs-test/new-game [{:keys [uid ?data] :as ev-msg}]
  (let [new-game-token (model/game-token 4)]
    (log/info
      "new-game:" new-game-token
      "initialized by:" ?data
      "with uid:" uid)
    (model/start-game! uid new-game-token ?data)
    ((:send-fn channel-socket) uid [:cs-test/new-game-initialized (get @model/app-state new-game-token)])
    (log/debug "current app-state:" @model/app-state)))

(defmethod event :cs-test/join-game [{:keys [uid ?data]}]
  (let [{:keys [player-name joining-game-token]} ?data
        game-state (get @model/app-state joining-game-token)]
    (when game-state
      (do
        (model/join-game! uid player-name joining-game-token)
        (let [players (get-in @model/app-state [joining-game-token :players])]
          (broadcast-game-state players [:cs-test/player-joined (get @model/app-state joining-game-token)]))))
    (log/debug "current app-state:" @model/app-state)))

(defmethod event :cs-test/end-game [{:keys [game-token]}]
  (log/info "ending game:" game-token)
  (model/end-game! game-token)
  (log/debug "current app-state:" @model/app-state))

(defmethod event :chsk/uidport-open [{:keys [uid client-id]}]
  (log/info "new connection:" client-id)
  (when uid
    (log/info "new uid:" uid)))

(defmethod event :chsk/uidport-close [{:keys [uid client-id]}]
  (log/info "close connection:" client-id)
  (when uid
    (log/info "dump this uid:" uid)))

(defmethod event :chsk/ws-ping [_])

(defn start-router []
  (log/info "Starting router...")
  (defonce router
    (sente/start-chsk-router! (:ch-recv channel-socket) event)))

(defn init []
  (start-websocket)
  (start-router))

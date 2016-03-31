(ns cartagena-cs.server.router
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
    [cartagena-cs.server.model :as model]))

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
      (reload/wrap-reload)
#_    (cond-> (environ/env :dev?) (reload/wrap-reload))
    (defaults/wrap-defaults (assoc-in defaults/site-defaults [:security :anti-forgery] false))
    (cors/wrap-cors :access-control-allow-origin [#".*"]
                    :access-control-allow-methods [:get :put :post :delete]
                    :access-control-allow-credentials ["true"])))

(defn broadcast-game-state [players event-and-payload]
  (doseq [player players]
    ((:send-fn channel-socket) (:uid player) event-and-payload)))

(defmulti event :id)

(defmethod event :default [{:keys [event]}]
  (log/info "Unhandled event: " event))

(defmethod event :cartagena-cs/new-game [{:keys [uid ?data] :as ev-msg}]
  (let [new-game-token (model/game-token)]
    (log/info
      "new-game:" new-game-token
      "initialized by:" ?data
      "with uid:" uid)
    (model/initialize-game! uid new-game-token ?data)
    ((:send-fn channel-socket) uid [:cartagena-cs/new-game-initialized (get @model/app-state new-game-token)])
    (log/debug "current app-state:" @model/app-state)))

(defmethod event :cartagena-cs/join-game [{:keys [uid ?data]}]
  (let [{:keys [player-name joining-game-token]} ?data
        game-state (get @model/app-state joining-game-token)]
    (when game-state
      (do
        (log/info "join-game:" uid player-name joining-game-token)
        (model/join-game! uid player-name joining-game-token)
        (let [players (get-in @model/app-state [joining-game-token :players])]
          (broadcast-game-state players [:cartagena-cs/player-joined (get @model/app-state joining-game-token)]))))
    (log/debug "current app-state:" @model/app-state)))

(defmethod event :cartagena-cs/start-game [{:keys [?data]}]
  (let [game-state (get @model/app-state ?data)]
    (when game-state
      (do
        (log/info "start-game:" ?data)
        (model/start-game! ?data)
        (let [players (get-in @model/app-state [?data :players])]
          (broadcast-game-state players [:cartagena-cs/game-started (get @model/app-state ?data)]))))))

(defmethod event :cartagena-cs/end-game [{:keys [token]}]
  (log/info "ending game:" token)
  (model/end-game! token)
  (log/debug "current app-state:" @model/app-state))

(defmethod event :cartagena-cs/update-active-player [{:keys [uid ?data]}]
  (model/update-current-player! uid ?data)
  (let [players (get-in @model/app-state [?data :players])]
    (broadcast-game-state players [:cartagena-cs/player-updated (get @model/app-state ?data)])))

(defmethod event :cartagena-cs/play-card [{:keys [uid ?data]}]
  (let [{:keys [token card from-space]} ?data]
    (model/play-card! uid token card from-space)
    (let [players (get-in @model/app-state [token :players])]
      (broadcast-game-state players [:cartagena-cs/card-played (get @model/app-state token)]))))

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

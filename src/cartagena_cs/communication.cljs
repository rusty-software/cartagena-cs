(ns cartagena-cs.communication
  (:require
    [cartagena-cs.auto-init]
    [cartagena-cs.config :as config]
    [cartagena-cs.model :as model]
    [taoensso.sente :as sente]))

(defn get-chsk-url
  "Connect to a configured server instead of the page host"
  [protocol chsk-host chsk-path type]
  (let [protocol (case type
                   :ajax protocol
                   :ws (if (= protocol "https:") "wss:" "ws:"))]
    (str protocol "//" config/server chsk-path)))

(defonce channel-socket
  (with-redefs [sente/get-chsk-url get-chsk-url]
    (sente/make-channel-socket! "/chsk" {:type :auto})))
(defonce chsk (:chsk channel-socket))
(defonce ch-chsk (:ch-recv channel-socket))
(defonce chsk-send! (:send-fn channel-socket))
(defonce chsk-state (:state channel-socket))

(defn new-game []
  (chsk-send! [:cartagena-cs/new-game (:player-name @model/game-state)]))

(defn start-game []
  (chsk-send! [:cartagena-cs/start-game (get-in @model/game-state [:server-state :token])]))

(defn end-game []
  (chsk-send! [:cartagena-cs/end-game (get-in @model/game-state [:server-state :token])]))

(defn join-game []
  (let [{:keys [player-name joining-game-token]} @model/game-state]
    (chsk-send! [:cartagena-cs/join-game {:player-name player-name
                                          :joining-game-token joining-game-token}])))

(defn update-active-player []
  (chsk-send! [:cartagena-cs/update-active-player (get-in @model/game-state [:server-state :token])]))

(defn play-card [card from-space]
  (chsk-send! [:cartagena-cs/play-card {:token (get-in @model/game-state [:server-state :token])
                                        :card card
                                        :from-space from-space}]))

(defn move-back [from-space]
  (chsk-send! [:cartagena-cs/move-back {:token (get-in @model/game-state [:server-state :token])
                                        :from-space from-space}]))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default [{:keys [event]}]
  (println "Unhandled event: %s" event))

(defmethod event-msg-handler :chsk/state [{:keys [?data]}]
  (if (= ?data {:first-open? true})
    (println "Channel socket successfully established!")
    (println "Channel socket state change:" ?data)))

(defmethod event-msg-handler :chsk/recv [{:keys [?data] :as msg}]
  (when-let [event (first ?data)]
    (case event
      :cartagena-cs/new-game-initialized (do
                                           (model/update-uid! (:uid @(:state msg)))
                                           (model/update-server-state! (second ?data)))
      :cartagena-cs/player-joined (do
                                    (model/update-uid! (:uid @(:state msg)))
                                    (model/update-server-state! (second ?data)))
      :cartagena-cs/game-started (model/update-server-state! (second ?data))
      :cartagena-cs/player-updated (model/update-server-state! (second ?data))
      :cartagena-cs/card-played (do
                                  (println "card-played")
                                  (model/card-played! (second ?data))
                                  (when (model/game-over?)
                                    (model/end-game!)))
      :cartagena-cs/moved-back (do
                                 (println "moved-back")
                                 (model/moved-back! (second ?data))
                                 #_(update-active-player))
      ))
  (println "recv from server:" ?data))

(defonce router
  (sente/start-client-chsk-router! ch-chsk event-msg-handler))



(ns cs-test.communication
  (:require
    [cs-test.auto-init]
    [cs-test.config :as config]
    [cs-test.model :as model]
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

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default [{:keys [event]}]
  (println "Unhandled event: %s" event))

(defmethod event-msg-handler :chsk/state [{:keys [?data]}]
  (if (= ?data {:first-open? true})
    (println "Channel socket successfully established!")
    (println "Channel socket state change:" ?data)))

(defmethod event-msg-handler :chsk/recv [{:keys [?data]}]
  (when-let [event (first ?data)]
    (case event
      :cs-test/new-game-initialized (model/update-game-token! (second ?data))
      ))
  (println "recv from server:" ?data))

(defonce router
  (sente/start-client-chsk-router! ch-chsk event-msg-handler))

(defn new-game []
  (chsk-send! [:cs-test/new-game (:player-name @model/game-state)]))

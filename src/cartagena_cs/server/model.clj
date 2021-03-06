(ns cartagena-cs.server.model
  (:require [cartagena-cs.server.game :as game]
            [taoensso.timbre :as log]))

(defonce app-state
  (atom {}))

(defn game-token []
  (Integer/toString (rand-int (Math/pow 36 6)) 36))

;; TODO: promote uid to keyword and string value, promote members of embedded map to first class of player
(comment
  {"gk12" {:token "gk12"
           :initialized-by "uid-123"
           :players [{:uid "uid-123" :name "tanya" :color :c1}
                     {:uid "uid-456" :name "rusty" :color :c2}]}})

(comment {:c1 ["gold" "goldenrod"]
          :c2 ["mediumorchid" "darkorchid"]
          :c3 ["orange" "darkorange"]
          :c4 ["mediumseagreen" "seagreen"]
          :c5 ["cornflowerblue" "midnightblue"]
          :c6 ["red" "darkred"]
          :c7 ["darkgray" "dimgray"]})

(def colors [:c1 :c2 :c3 :c4 :c5 :c6 :c7])

(defn initialize-game [app-state uid token name]
  (let [color (rand-nth colors)
        remaining-colors (remove #{color} colors)]
    (assoc app-state token {:token token
                            :initialized-by uid
                            :remaining-colors remaining-colors
                            :players [{:uid uid
                                       :name name
                                       :color color}]})))

(defn initialize-game! [uid token name]
  (swap! app-state initialize-game uid token name))

(defn end-game! [token]
  (swap! app-state dissoc token))

(defn join-game [app-state uid name token]
  (let [game-state (get app-state token)
        players (:players game-state)
        colors (:remaining-colors game-state)
        color (rand-nth colors)
        remaining-colors (remove #{color} colors)
        new-players (conj players {:uid uid
                                   :name name
                                   :color color})
        game-state (assoc game-state :players new-players
                                     :remaining-colors remaining-colors)]
    (assoc app-state token game-state)))

(defn join-game! [uid name token]
  (swap! app-state join-game uid name token))

(defn start-game [app-state token]
  (let [game-state (get app-state token)
        initialized-game (game/initialize-game (:players game-state))]
    (assoc app-state token (merge game-state initialized-game {:game-on? true}))))

(defn start-game! [token]
  (swap! app-state start-game token))

(defn update-current-player [app-state uid token]
  (let [game-state (get app-state token)]
    (if (= uid (:current-player game-state))
      (let [{:keys [current-player actions-remaining]} (game/update-current-player
                                                         (:actions-remaining game-state)
                                                         uid
                                                         (:player-order game-state))
            game-state (assoc game-state :current-player current-player
                                         :actions-remaining actions-remaining)]
        (log/debug "update-current-player:" current-player actions-remaining)
        (assoc app-state token game-state))
      (assoc app-state token game-state))))

(defn update-current-player! [uid token]
  (swap! app-state update-current-player uid token))

(defn player-by-uid [game-state uid]
  (let [players (:players  game-state)]
    (first (filter #(= uid (:uid %)) players))))

(defn play-card [app-state uid token card from-space]
  (let [game-state (get app-state token)]
    (if (= uid (:current-player game-state))
      (let [current-player (player-by-uid game-state uid)
            {:keys [player board discard-pile]} (game/play-card
                                                  current-player
                                                  card
                                                  from-space
                                                  (:board game-state)
                                                  (:discard-pile game-state))
            game-state (assoc game-state :players (conj (remove #(= uid (:uid %)) (:players game-state)) player)
                                         :board board
                                         :discard-pile discard-pile)]
        (assoc app-state token game-state))
      app-state)))

(defn play-card! [uid token card from-space]
  (swap! app-state play-card uid token card from-space)
  (update-current-player! uid token))

(defn move-back [app-state uid token from-space]
  (let [game-state (get app-state token)]
    (if (= uid (:current-player game-state))
      (let [current-player (player-by-uid game-state uid)
            {:keys [player board draw-pile discard-pile]} (game/move-back
                                                            current-player
                                                            from-space
                                                            (:board game-state)
                                                            (:draw-pile game-state)
                                                            (:discard-pile game-state))
            game-state (assoc game-state :players (conj (remove #(= uid (:uid %)) (:players game-state)) player)
                                         :board board
                                         :draw-pile draw-pile
                                         :discard-pile discard-pile)]
        (assoc app-state token game-state))
      app-state)))

(defn move-back! [uid token from-space]
  (swap! app-state move-back uid token from-space)
  (update-current-player! uid token))

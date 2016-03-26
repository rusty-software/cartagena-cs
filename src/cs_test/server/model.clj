(ns cs-test.server.model)

(defonce app-state
  (atom {}))

(defn game-token [n]
  (let [chars (map char (range 65 127))
        token (take n (repeatedly #(rand-nth chars)))]
    (reduce str token)))

(defn start-game! [uid token name]
  (swap! app-state assoc token {:token token
                                :initialized-by uid
                                :players [{uid name}]}))

(defn end-game! [token]
  (swap! app-state dissoc token))

(defn join-game [game-state uid name token]
  (let [players (:players (get game-state token))
        new-players (conj players {uid name})]
    (assoc-in game-state [token :players] new-players)))

(defn join-game! [uid name token]
  (swap! app-state join-game uid name token))

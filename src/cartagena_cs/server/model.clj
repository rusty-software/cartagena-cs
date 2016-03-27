(ns cartagena-cs.server.model)

(defonce app-state
  (atom {}))

(defn game-token [n]
  (let [chars (map char (range 65 127))
        token (take n (repeatedly #(rand-nth chars)))]
    (reduce str token)))

(comment
  {"gk12" {:token "gk12"
           :initialized-by "uid-123"
           :players [{"uid-123" {:name "rusty"}}
                     {"uid-456" {:name "tanya"}}]}})

(defn initialize-game! [uid token name]
  (swap! app-state assoc token {:token token
                                :initialized-by uid
                                :players [{uid {:name name}}]}))

(defn end-game! [token]
  (swap! app-state dissoc token))

(defn join-game [game-state uid name token]
  (let [players (:players (get game-state token))
        new-players (conj players {uid {:name name}})]
    (assoc-in game-state [token :players] new-players)))

(defn join-game! [uid name token]
  (swap! app-state join-game uid name token))

(defn start-game! [token]
  (swap! app-state assoc-in [token :game-on?] true))

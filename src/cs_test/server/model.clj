(ns cs-test.server.model)

(defonce app-state
  (atom {}))

(defn game-token [n]
  (let [chars (map char (range 48 127))
        token (take n (repeatedly #(rand-nth chars)))]
    (reduce str token)))

(defn join-game [m uid name token]
  )

(defn join-game! [uid name token]
  (swap! app-state join-game uid name token))

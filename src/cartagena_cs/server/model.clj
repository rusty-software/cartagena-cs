(ns cartagena-cs.server.model)

(defonce app-state
  (atom {}))

(defn game-token []
  (Integer/toString (rand-int (Math/pow 36 6)) 36))

(comment
  {"gk12" {:token "gk12"
           :initialized-by "uid-123"
           :players [{"uid-123" {:name "rusty" :color :c1}}
                     {"uid-456" {:name "tanya" :color :c2}}]}})

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
                            :players [{uid {:name name
                                            :color color}}]})))

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
        new-players (conj players {uid {:name name
                                        :color color}})
        game-state (assoc game-state :players new-players
                                     :remaining-colors remaining-colors)]
    (assoc app-state token game-state)))

(defn join-game! [uid name token]
  (swap! app-state join-game uid name token))

(defn start-game! [token]
  (swap! app-state assoc-in [token :game-on?] true))

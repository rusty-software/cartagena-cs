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
           :players [{"uid-123" {:name "rusty" :color :brown}}
                     {"uid-456" {:name "tanya" :color :orange}}]}})

(comment {:yellow ["gold" "goldenrod"]
          :brown ["peru" "saddlebrown"]
          :orange ["orange" "darkorange"]
          :green ["mediumseagreen" "seagreen"]
          :blue ["cornflowerblue" "midnightblue"]
          :red ["red" "darkred"]
          :gray ["darkgray" "dimgray"]})

(defn initialize-game [app-state uid token name]
  (let [colors [:yellow :brown :orange :green :blue :red :gray]
        color (rand-nth colors)
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

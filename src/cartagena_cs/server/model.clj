(ns cartagena-cs.server.model
  (:require [cartagena-cs.server.game :as game]))

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

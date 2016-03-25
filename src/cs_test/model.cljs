(ns cs-test.model
  (:require
    [reagent.core :as reagent]))

(defonce game-state (reagent/atom {}))

(defn update-player-name [gs name]
  (assoc gs :player-name name))

(defn update-player-name! [name]
  (swap! game-state update-player-name name))

(defn update-game-token [gs token]
  (assoc gs :game-token token))

(defn update-game-token! [token]
  (swap! game-state update-game-token token))

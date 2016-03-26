(ns cs-test.model
  (:require
    [reagent.core :as reagent]))

(defonce game-state (reagent/atom {}))

(defn update-player-name! [name]
  (swap! game-state assoc :player-name name))

(defn update-game-token! [token]
  (swap! game-state assoc :game-token token))

(defn update-joining-game-token! [token]
  (swap! game-state :joining-game-token token))

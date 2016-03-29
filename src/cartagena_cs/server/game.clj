(ns cartagena-cs.server.game
  (:require
    [schema.core :as s]
    [cartagena-cs.server.schema :as schema]))

(def board-piece1 [:bottle :gun :hat :skull :knife :key])
(def board-piece2 [:knife :bottle :key :gun :hat :skull])
(def board-piece3 [:hat :key :gun :bottle :skull :knife])
(def board-piece4 [:key :bottle :skull :knife :hat :gun])
(def board-piece5 [:gun :key :knife :hat :skull :bottle])
(def board-piece6 [:hat :knife :key :bottle :gun :skull])
(def all-board-pieces [board-piece1 board-piece2 board-piece3 board-piece4 board-piece5 board-piece6])

(def icons [:bottle :gun :hat :key :knife :skull])

(defn initialize-board
  "Generates a board from 6 random pieces, adding a jail and ship to the beginning and end, respectively."
  []
  (let [space-icons (-> (for [piece all-board-pieces]
                          (if (zero? (rand-int 2))
                            piece
                            (reverse piece)))
                        flatten
                        (conj :jail)
                        (concat [:ship])
                        vec)]
    (vec (for [index (range 0 38)
               :let [space-icon (get space-icons index)]]
           {:index index :icon space-icon :pirates []}))))

(defn shuffle-cards
  "Shuffles and returns passed cards"
  [cards]
  (shuffle cards))

(defn initialize-cards
  "Creates a collection of 17 of each icon, shuffles, and returns them."
  []
  (->> icons
       (map #(repeat 17 %))
       flatten
       shuffle-cards
       vec))

(defn draw-cards
  "Draws cards off of the draw pile and puts them in the player's hand.  If there aren't enough cards in the draw pile, the discard pile is shuffled into the draw pile.  Returns a map of the affected player, draw pile, and discard pile."
  [n player draw-pile discard-pile]
  (if (< (count draw-pile) n)
    (let [drawn-cards draw-pile
          draw-pile (shuffle-cards discard-pile)
          left-to-draw (- n (count drawn-cards))
          more-cards (vec (take left-to-draw draw-pile))
          draw-pile (vec (drop left-to-draw draw-pile))
          all-drawn (apply conj drawn-cards more-cards)]
      {:player (assoc player :cards (apply conj (:cards player) all-drawn))
       :draw-pile draw-pile
       :discard-pile []})
    {:player (assoc player :cards (apply conj (:cards player) (take n draw-pile)))
     :draw-pile (vec (drop n draw-pile))
     :discard-pile discard-pile}))

(s/defn initialize-game
  "Initializes a new game by creating a board, setting up players with 6 cards and pirates in jail, replacing the draw pile, setting the player order and current player.  Returns a map of the game state."
  [players :- [schema/Player]]
  (let [board (initialize-board)
        players-draw-pile (loop [no-card-players (vec (map #(assoc % :cards []) players))
                                 cards (initialize-cards)
                                 players-with-cards []]
                            (if (empty? no-card-players)
                              players-with-cards
                              (let [player-draw-pile (draw-cards 6 (first no-card-players) cards [])]
                                (recur (rest no-card-players) (:draw-pile player-draw-pile) (conj players-with-cards player-draw-pile)))))
        init-players (vec (map :player players-draw-pile))
        draw-pile (:draw-pile (last players-draw-pile))
        pirates-in-jail (vec (flatten (map #(repeat 6 %) (map :color players))))
        board (assoc board 0 {:index 0 :icon :jail :pirates pirates-in-jail})]
    {:board board
     :players init-players
     :player-order (vec (map :name init-players))
     :current-player (:name (first init-players))
     :actions-remaining 3
     :draw-pile draw-pile
     :discard-pile []}))



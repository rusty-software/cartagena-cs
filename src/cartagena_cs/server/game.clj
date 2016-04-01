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
     :player-order (vec (map :uid init-players))
     :current-player (:uid (first init-players))
     :actions-remaining 3
     :draw-pile draw-pile
     :discard-pile []}))

(defn next-player
  "Returns the player string whose turn is... well, next."
  [current-player player-order]
  (let [current-player-index (.indexOf player-order current-player)]
    (if (= current-player-index (dec (count player-order)))
      (get player-order 0)
      (get player-order (inc current-player-index)))))

(defn update-current-player
  "Decrements the moves remaining until the value reaches 0. Rotates the current player and resets the moves count at that point.  Returns the current player string and actions remaining count."
  [actions-remaining current-player player-order]
  (let [actions-remaining (dec actions-remaining)]
    (if (zero? actions-remaining)
      {:current-player (next-player current-player player-order)
       :actions-remaining 3}
      {:current-player current-player
       :actions-remaining actions-remaining})))

(defn remove-pirate-from-space
  "Returns a space with a single instand of the target pirate removed from the pirates collection."
  [color space]
  (let [[pre-pirates post-pirates] (split-with #(not= color %) (:pirates space))]
    (assoc space :pirates (vec (flatten (concat pre-pirates (rest post-pirates)))))))

(defn is-open-target?
  "Returns true if the space matches the icon and has fewer than three pirates; otherwise false."
  [space icon]
  (and (= icon (:icon space))
       (< (count (:pirates space)) 3)))

(defn open-space-index
  "Returns the index of the first open space for the given icon after the starting index."
  [starting-index board icon]
  (or
    (some #(let [space (get board %)]
            (when (is-open-target? space icon) %))
          (range (inc starting-index) (count board)))
    (dec (count board))))

(defn add-pirate-to-space
  "Returns a space with the target pirate added to the pirates collection."
  [color space]
  (update-in space [:pirates] conj color))

(defn play-card
  "Discards the card and moves a single pirate from the source space to the next available icon space.  Returns the updated player, board, and discard pile."
  [player icon from-space board discard-pile]
  (let [[pre-cards post-cards] (split-with #(not= icon %) (:cards player))
        updated-from-space (remove-pirate-from-space (:color player) from-space)
        space-index (.indexOf board from-space)
        next-open-space-index (open-space-index space-index board icon)
        next-open-space (get board next-open-space-index)
        updated-target-space (add-pirate-to-space (:color player) next-open-space)]
    {:player (assoc player :cards (concat pre-cards (rest post-cards)))
     :board (assoc board space-index updated-from-space
                                next-open-space-index updated-target-space)
     :discard-pile (conj discard-pile icon)}))

(defn occupiable-space-index
  "Returns the index of the first space with either one or two pirates before the starting index."
  [starting-index board]
  (some #(let [space (get board %)
               pirate-count (count (:pirates space))]
          (when (or (= 1 pirate-count) (= 2 pirate-count)) %))
        (range (dec starting-index) 0 -1)))

(defn move-back
  "Moves a single pirate back to the first occupiable space.  Returns the updated player, board, draw, and discard piles."
  [player from-space board draw-pile discard-pile]
  (if-let [prev-occupiable-space-index (occupiable-space-index (.indexOf board from-space) board)]
    (let [from-space-index (.indexOf board from-space)
          target-space (get board prev-occupiable-space-index)
          draw-count (count (:pirates target-space))
          {:keys [player draw-pile discard-pile]} (draw-cards draw-count player draw-pile discard-pile)
          updated-from-space (remove-pirate-from-space (:color player) from-space)
          updated-target-space (add-pirate-to-space (:color player) target-space)]
      {:player player
       :board (assoc board from-space-index updated-from-space
                           prev-occupiable-space-index updated-target-space)
       :draw-pile draw-pile
       :discard-pile discard-pile})
    {:player player
     :board board
     :draw-pile draw-pile
     :discard-pile discard-pile}))

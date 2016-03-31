(ns cartagena-cs.view
  (:require
    [cartagena-cs.communication :as communication]
    [cartagena-cs.constants :as constants]
    [cartagena-cs.model :as model]
    [cljs.pprint :as pprint]))

(defn to-scale [n]
  (* 1.65 n))

(defn name-input []
  [:div
   [:h4
    "Name, please:"
    [:input
     {:id "txt-playername"
      :type "text"
      :value (:player-name @model/game-state)
      :on-change #(model/update-player-name! (-> % .-target .-value))}]]])

(defn new-game-button []
  [:button
   {:id "btn-new-game"
    :class "button brown"
    :on-click #(communication/new-game)}
   "New Game"])

(defn start-game-button []
  [:button
   {:id "btn-start-game"
    :class "button yellow"
    :on-click #(communication/start-game)}
   "Start Game"])

(defn end-game-button []
  [:button
   {:id "btn-end-game"
    :class "button yellow"
    :on-click #(communication/end-game)}
   "End Game"])

(defn color-marker [color size loc r w]
  (let [stroke (second (color constants/colors))
        fill (first (color constants/colors))]
    [:svg
     {:height (to-scale size)
      :width (to-scale size)}
     [:circle
      {:cx (to-scale loc)
       :cy (to-scale loc)
       :r (to-scale r)
       :stroke stroke
       :stroke-width (to-scale w)
       :fill fill}]]))

(defn initializing-table-rows []
  [[:tr
    {:key "token-row"}
    [:td
     [:span
      "Token: "]
     [:span
      {:class "leading"}
      (get-in @model/game-state [:server-state :token])]]]
   [:tr
    {:key "players-row"}
    [:td
     "Current players:"
     [:br]
     [:ul
      (for [player (get-in @model/game-state [:server-state :players])]
        ^{:key player}
        [:li (:name player)
         [color-marker (:color player) 12 6 5 1]])]]]
   (when (not (:joining-game-token @model/game-state))
     [:tr
      {:key "start-button-row"}
      [:td
       {:style {:text-align "center"}}
       [start-game-button]]])])

(defn start-a-game []
  [:div
   {:style {:display "inline-block"
            :vertical-align "top"
            :margin "5px 5px 5px 5px"}}
   [:table
    [:thead
     [:tr
      [:th
       {:style {:text-align "center"}}
       "Starting a game"]]]
    [:tbody
     (let [server-state (:server-state @model/game-state)]
       (if (not server-state)
         [:tr
          [:td
           {:style {:text-align "center"}}
           [new-game-button]]]
         (for [row (initializing-table-rows)]
           ^{:key row}
           row)))]]])

(defn join-a-game []
  [:div
   {:style {:display "inline-block"
            :vertical-align "top"
            :margin "5px 5px 5px 5px"}}
   [:table
    [:tr
     [:th
      {:style {:text-align "center"}}
      "Joining a game"]]
    [:tr
     [:td
      {:style {:text-align "center"}}
      [:text "Enter game code:"]
      [:input
       {:id "txt-game-token"
        :type "text"
        :value (:joining-game-token @model/game-state)
        :on-change #(model/update-joining-game-token! (-> % .-target .-value))}]]]
    [:tr
     [:td
      {:style {:text-align "center"}}
      [:button
       {:id "btn-join-game"
        :class "button brown"
        :on-click #(communication/join-game)}
       "Join Game"]]]]])

(defn static-board []
  [;; jail
   [:rect
    {:x 0
     :y 0
     :width (to-scale 50)
     :height (to-scale 90)
     :stroke "black"
     :fill "darkgray"
     :fill-opacity 0.5}]
   [:g
    {:dangerouslySetInnerHTML {:__html (str "<image xlink:href=\"img/jail.png\" x=0 y=0 width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}]
   ;; ship
   [:rect
    {:x (to-scale 410)
     :y (to-scale 240)
     :width (to-scale 80)
     :height (to-scale 60)
     :stroke "black"
     :fill "sienna"
     :fill-opacity 0.5}]
   [:g
    {:dangerouslySetInnerHTML {:__html (str "<image xlink:href=\"img/ship.png\" x=\"" (to-scale 410) "\" y=\"" (to-scale 240) "\" width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}]
   ])

(defn normal-space [x y]
  [:rect
   {:x (to-scale x)
    :y (to-scale y)
    :width (to-scale 40)
    :height (to-scale 30)
    :stroke "black"
    :stroke-width "0.5"
    :fill "lightgray"
    :fill-opacity 0.5}])

(defn space-image [x y icon]
  [:g
   {:dangerouslySetInnerHTML
    {:__html (str "<image xlink:href=\"" (icon constants/icon-images) "\" x=\"" (to-scale x) "\" y=\"" (to-scale y) "\" width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}])

(defn player [uid]
  (let [players (get-in @model/game-state [:server-state :players])]
    (first (filter #(= uid (:uid %)) players))))

(defn current-player []
  (player (get-in @model/game-state [:server-state :current-player])))

(defn my-turn? []
  (= (:uid @model/game-state) (:uid (current-player))))

(defn clicked-my-pirate? [color]
  (= color (:color (player (:uid @model/game-state)))))

(defn pirate-click [color from-space-index]
  (when (and (my-turn?)
             (clicked-my-pirate? color))
    (let [board (get-in @model/game-state [:server-state :board])
          from-space (get board from-space-index)]
      (println "from-space" from-space)
      (if-let [selected-card (:selected-card @model/game-state)]
        (communication/play-card selected-card from-space)
        (println "moving back"))))
  #_(when (= color (:color (active-player @app-state)))
      (let [player (active-player @app-state)
            board (:board @app-state)
            from-space (get board from-space-index)
            discard-pile (:discard-pile @app-state)]
        (if-let [selected-card (:selected-card @app-state)]
          (play-card! player selected-card from-space board discard-pile)
          (move-back! player from-space board (:draw-pile @app-state) discard-pile)))))

(defn circles-for [space-index x y colors]
  (for [color-index (range (count colors))
        :let [color (get colors color-index)
          stroke (second (color constants/colors))
          fill (first (color constants/colors))
          cx (to-scale (+ 35 x))
          cy (to-scale (+ y 5 (* 10 color-index)))]]
    ^{:key color-index}
    [:circle
       {:cx cx
        :cy cy
        :r (to-scale 4)
        :stroke stroke
        :stroke-width (to-scale 1)
        :fill fill
        :on-click #(pirate-click color space-index)}]))

(defn normal-spaces []
  (apply concat
         (for [i (range 1 37)]
           (when-let [space-data (get-in @model/game-state [:server-state :board i])]
             (let [position (get constants/piece-positions i)
                   x (:x position)
                   y (:y position)
                   space (normal-space x y)
                   image (space-image x y (:icon space-data))
                   pirates (circles-for i x y (:pirates space-data))]
               (conj [space image] pirates))))))



(defn special-space [space-index x-base y-base]
  (when-let [special-space (get-in @model/game-state [:server-state :board space-index])]
    (apply concat
      (let [pirate-frequencies (frequencies (:pirates special-space))
            pirate-colors (vec (keys pirate-frequencies))]
        (for [player-index (range (count pirate-frequencies))
              :let [pirate-color (get pirate-colors player-index)
                    pirate-count (pirate-color pirate-frequencies)
                    stroke (second (pirate-color constants/colors))
                    fill (first (pirate-color constants/colors))
                    x (to-scale (+ x-base (* 10 player-index)))]]
          (for [pirate-index (range pirate-count)
                :let [y (to-scale (+ y-base (* 10 pirate-index)))]]
              [:circle
                 {:cx x
                  :cy y
                  :r (to-scale 4)
                  :stroke stroke
                  :stroke-width (to-scale 1)
                  :fill fill
                  :on-click #(pirate-click pirate-color space-index)}]))))))

(defn jail []
  (special-space 0 5 35))

(defn ship []
  (special-space 37 445 245))

(defn game-area []
  [:div
   {:style {:display "inline-block"
            :vertical-align "top"
            :margin "5px 5px 5px 5px"}}
   (-> [:svg
        {:view-box (str "0 0 " (to-scale 501) " " (to-scale 301))
         :width (to-scale 501)
         :height (to-scale 301)}]
       (into (static-board))
       (into (normal-spaces))
       (into (jail))
       (into (ship)))
   ])

(defn player-area []
  (let [{:keys [cards color] player-name :name} (player (:uid @model/game-state))
        card-groups (frequencies cards)]
    [:div
     {:style {:display "inline-block"
              :vertical-align "top"
              :margin "5px 5px 5px 5px"}}
     [:table
      {:style {:width "540px"}}
      (when (my-turn?)
        [:thead
         [:tr
          [:th {:col-span 2
                :style {:text-align "center"}}
           "It's your turn!"]]
         [:tr
          [:td {:class "bold"} "Actions"]
          [:td (get-in @model/game-state [:server-state :actions-remaining])
           [:button
            {:class "button brown"
             :style {:margin "0 0 0 20px"}
             :on-click #(communication/update-active-player)} "Pass"]]]])
      [:tbody
       [:tr
        [:td {:class "bold"} "Name"]
        [:td player-name]]
       [:tr
        [:td {:class "bold"} "Color"]
        [:td
         [color-marker color 20 10 7 2]]]
       [:tr
        [:td {:class "bold"} "Cards"]
        [:td (for [[card num] card-groups]
               ^{:key card}
               [:span {:style {:float "left"}}
                [:figure
                 [:img
                  {:src (card constants/icon-images)
                   :width (to-scale 30)
                   :height (to-scale 30)
                   :on-click #(model/select-card! card)}]
                 [:center [:figcaption num]]]])]]
       [:tr
        {:style {:height (to-scale 50)}}
        [:td {:class "bold"} "Selected Card"]
        [:td
         [:span {:style {:float "left"}}
          (when-let [selected-card (:selected-card @model/game-state)]
            [:img
             {:src (selected-card constants/icon-images)
              :width (to-scale 30)
              :height (to-scale 30)
              :on-click #(model/unselect-card!)}])]]]
       [:tr
        [:td {:class "bold"
              :col-span 2
              :style {:text-align "center"}}
         "On your turn..."]]
       [:tr
        [:td {:col-span 2}
         "To move forward, click a card, then click the target pirate.  To undo card selection, click the selected card."]]
       [:tr
        [:td {:col-span 2}
         "To move backward, click the target pirate."]]]]
     ]))

(defn main []
  [:center
   [:div
    [:h1 "Cartagena Client>Server"]
    (if (get-in @model/game-state [:server-state :game-on?])
      [:div
       [game-area]
       [player-area]]
      [:div
       [name-input]
       [start-a-game]
       (when (not (:server-state @model/game-state))
         [join-a-game])])

    [:hr]
    [:div
     [:span
      "Client game state: " (with-out-str (pprint/pprint @model/game-state))]]]])

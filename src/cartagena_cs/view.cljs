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

(defn color-marker [color]
  (let [stroke (second (color constants/colors))
        fill (first (color constants/colors))]
    [:svg
     {:height "20px"
      :width "20px"}
     [:circle
      {:cx (to-scale 6)
       :cy (to-scale 6)
       :r (to-scale 5)
       :stroke stroke
       :stroke-width (to-scale 1)
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
      (for [uid-player (get-in @model/game-state [:server-state :players])
            :let [player (first (vals uid-player))]]
        ^{:key player}
        [:li (:name player)
         [color-marker (:color player)]])]]]
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
     :fill "darkgray"}]
   [:g
    {:dangerouslySetInnerHTML {:__html (str "<image xlink:href=\"img/jail.png\" x=0 y=0 width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}]
   ;; ship
   [:rect
    {:x (to-scale 410)
     :y (to-scale 240)
     :width (to-scale 80)
     :height (to-scale 60)
     :stroke "black"
     :fill "sienna"}]
   [:g
    {:dangerouslySetInnerHTML {:__html (str "<image xlink:href=\"img/ship.png\" x=\"" (to-scale 410) "\" y=\"" (to-scale 240) "\" width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}]
   ])

#_(defn normal-spaces []
  (apply concat
         (for [i (range 1 37)]
           (when-let [space-data (get-in @app-state [:board i])]
             (let [position (get piece-positions i)
                   x (:x position)
                   y (:y position)
                   space (normal-space x y)
                   image (space-image x y (:icon space-data))
                   pirates (circles-for i x y (:pirates space-data))]
               (conj [space image] pirates))))))

(defn game-area []
  [:div
   (-> [:svg
        {:view-box (str "0 0 " (to-scale 501) " " (to-scale 301))
         :width (to-scale 501)
         :height (to-scale 301)}]
       (into (static-board)))
   ])

(defn main []
  [:center
   [:div
    [:h1 "Cartagena Client>Server"]
    (if (get-in @model/game-state [:server-state :game-on?])
      [game-area]
      [:div
       [name-input]
       [start-a-game]
       (when (not (:server-state @model/game-state))
         [join-a-game])])

    [:hr]
    [:div
     [:span
      "Client game state: " (with-out-str (pprint/pprint @model/game-state))]]]])

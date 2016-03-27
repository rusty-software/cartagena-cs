(ns cartagena-cs.view
  (:require
    [cartagena-cs.communication :as communication]
    [cartagena-cs.model :as model]
    [cljs.pprint :as pprint]))

(defn to-scale [n]
  (* 1.65 n))

(def colors {:yellow ["gold" "goldenrod"]
             :brown ["peru" "saddlebrown"]
             :orange ["orange" "darkorange"]
             :green ["mediumseagreen" "seagreen"]
             :blue ["cornflowerblue" "midnightblue"]
             :red ["red" "darkred"]
             :gray ["darkgray" "dimgray"]})

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
  (let [stroke (second (color colors))
        fill (first (color colors))]
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

(defn main []
  [:center
   [:div
    [:h1 "Cartagena Client>Server"]
    (when (not (get-in @model/game-state [:server-state :game-on?]))
      [:div
       [name-input]
       [start-a-game]
       (when (not (:server-state @model/game-state))
         [join-a-game])])
    [:hr]
    [:div
     [:span
      "Client game state: " (with-out-str (pprint/pprint @model/game-state))]]]])

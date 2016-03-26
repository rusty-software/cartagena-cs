(ns cs-test.view
  (:require
    [cs-test.communication :as communication]
    [cs-test.model :as model]))

(defn display-game-token [game-token]
  [:div
   [:span
    "Your game token is: " game-token]])

(defn display-join-game []
  [:div
   [:hr]
   [:text "Enter game code:"]
   [:input
    {:id "txt-game-token"
     :type "text"
     :value (:joining-game-token @model/game-state)
     :on-change #(model/update-joining-game-token! (-> % .-target .-value))}]
   [:button
    {:id "btn-join-game"
     :on-click #(communication/join-game)}
    "Join Game"]])

(defn main []
  [:div
   [:h1 "CS Test"]
   [:hr]
   [:div
    {:id "new-game"}
    [:text "Name, please:"]
    [:input
     {:id "txt-playername"
      :type "text"
      :value (:player-name @model/game-state)
      :on-change #(model/update-player-name! (-> % .-target .-value))}]
    [:hr]
    [:button
     {:id "btn-new-game"
      :on-click #(communication/new-game)}
     "New Game"]
    (let [game-token (:game-token @model/game-state)]
      (if game-token
        (display-game-token game-token)
        (display-join-game)))]])

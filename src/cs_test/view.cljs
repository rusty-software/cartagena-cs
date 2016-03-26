(ns cs-test.view
  (:require
    [cs-test.communication :as communication]
    [cs-test.model :as model]
    [cljs.pprint :as pprint]))

(defn display-server-state [server-state]
  [:div
   [:span
    "Your server state is: " (with-out-str (pprint/pprint server-state))]])

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
    (let [server-state (:server-state @model/game-state)]
      (if server-state
        (display-server-state server-state)
        (display-join-game)))]

   [:hr]
   [:div
    [:span
     "Client game state: " (with-out-str (pprint/pprint @model/game-state))]]])

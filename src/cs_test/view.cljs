(ns cs-test.view
  (:require
    [cs-test.communication :as communication]
    [cs-test.model :as model]))

(defn main []
  [:div
   [:h1 "CS Test"]
   [:hr]
   [:div
    {:id "new-game"}
    [:input
     {:id "txt-ng-playername"
      :type "text"
      :value (:player-name @model/game-state)
      :on-change #(model/update-player-name! (-> % .-target .-value))}]
    [:button
     {:id "btn-new-game"
      :on-click #(communication/new-game)}
     "New Game"]]
   [:div
    (when-let [game-token (:game-token @model/game-state)]
      [:span
       "Your game token is: " game-token])]])

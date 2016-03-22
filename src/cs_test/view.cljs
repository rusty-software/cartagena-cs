(ns cs-test.view
  (:require
    [cs-test.model :as model]))

(defn main []
  [:div
   [:h1 "CS Test"]
   [:button
     {:id "btn-new-game"
      :on-click #(model/new-game!)}
     "New Game"]])

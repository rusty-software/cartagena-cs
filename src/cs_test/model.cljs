(ns cs-test.model
  (:require
    [cs-test.communication :as communication]))

(defn new-game! []
  (communication/new-game))

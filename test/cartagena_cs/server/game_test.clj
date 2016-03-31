(ns cartagena-cs.server.game-test
  (:require [clojure.test :refer :all]
            [cartagena-cs.server.game :refer :all]))

(deftest next-player-test
  (is (= "rusty" (next-player "tanya" ["tanya" "rusty"])))
  (is (= "tanya" (next-player "rusty" ["tanya" "rusty"])))
  (is (= "one player" (next-player "one player" ["one player"]))))

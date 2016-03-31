(ns cartagena-cs.server.model-test
  (:require [clojure.test :refer :all]
            [cartagena-cs.server.model :refer :all]))

(deftest update-current-player-test
  (testing "Given a player list with one player, resets the current player and actions remaining correctly"
    (let [app-state {"abc123" {:player-order ["one player uid"]
                               :players [{:uid "one player uid"}]
                               :actions-remaining 1
                               :current-player "one player uid"}}
          expected {"abc123" {:player-order ["one player uid"]
                               :players [{:uid "one player uid"}]
                               :actions-remaining 3
                               :current-player "one player uid"}}]
      (is (= expected (update-current-player app-state "one player uid" "abc123"))))))

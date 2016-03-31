(ns cartagena-cs.server.model-test
  (:require [clojure.test :refer :all]
            [cartagena-cs.server.model :refer :all]))

(deftest update-current-player-test
  (testing "decrements moves remaining"
    (let [app-state {"abc123" {:player-order ["tanyauid" "rustyuid"]
                               :players [{:uid "tanyauid"} {:uid "rustyuid"}]
                               :actions-remaining 3
                               :current-player "tanyauid"}}
          expected {"abc123" {:player-order ["tanyauid" "rustyuid"]
                               :players [{:uid "tanyauid"} {:uid "rustyuid"}]
                               :actions-remaining 2
                               :current-player "tanyauid"}}]
      (is (= expected (update-current-player app-state "tanya" "abc123")))))
  #_(testing "rotates player and resets move count"
    (let [expected {:current-player "rusty"
                    :actions-remaining 3}]
      (is (= expected (update-current-player 1 "tanya" ["tanya" "rusty"]))))))


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

(ns cartagena-cs.constants)

(def colors {:c1 ["gold" "goldenrod"]
             :c2 ["mediumorchid" "darkorchid"]
             :c3 ["orange" "darkorange"]
             :c4 ["mediumseagreen" "seagreen"]
             :c5 ["cornflowerblue" "midnightblue"]
             :c6 ["red" "darkred"]
             :c7 ["darkgray" "dimgray"]})

(def icon-images {:jail "img/jail.png"
                  :ship "img/ship.png"
                  :bottle "img/bottle.png"
                  :gun "img/gun.jpg"
                  :hat "img/hat.png"
                  :key "img/key.png"
                  :knife "img/knife.png"
                  :skull "img/skull.png"})

(def piece-positions
  [
   ;; jail
   {:x 0 :y 0}
   ;; row 1, left to right
   {:x 50 :y 60}
   {:x 90 :y 60}
   {:x 130 :y 60}
   {:x 170 :y 60}
   {:x 210 :y 60}
   {:x 250 :y 60}
   {:x 290 :y 60}
   {:x 330 :y 60}
   {:x 370 :y 60}
   {:x 410 :y 60}
   {:x 450 :y 60}
   ;; transition 1
   {:x 450 :y 90}
   ;; row 2, right to left
   {:x 450 :y 120}
   {:x 410 :y 120}
   {:x 370 :y 120}
   {:x 330 :y 120}
   {:x 290 :y 120}
   {:x 250 :y 120}
   {:x 210 :y 120}
   {:x 170 :y 120}
   {:x 130 :y 120}
   {:x 90 :y 120}
   {:x 50 :y 120}
   ;; transition 2
   {:x 50 :y 150}
   ;; row 3, left to right
   {:x 50 :y 180}
   {:x 90 :y 180}
   {:x 130 :y 180}
   {:x 170 :y 180}
   {:x 210 :y 180}
   {:x 250 :y 180}
   {:x 290 :y 180}
   {:x 330 :y 180}
   {:x 370 :y 180}
   {:x 410 :y 180}
   {:x 450 :y 180}
   ;; transition 3
   {:x 450 :y 210}
   ;; ship
   {:x 400 :y 240}])

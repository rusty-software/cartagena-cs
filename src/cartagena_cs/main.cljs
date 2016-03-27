(ns cartagena-cs.main
  (:require
    [cartagena-cs.auto-init]
    [cartagena-cs.view :as view]
    [reagent.core :as reagent]))

(reagent/render-component
  [view/main]
  (. js/document (getElementById "app")))


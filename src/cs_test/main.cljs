(ns cs-test.main
  (:require
    [cs-test.auto-init]
    [cs-test.view :as view]
    [reagent.core :as reagent]))

(reagent/render-component
  [view/main]
  (. js/document (getElementById "app")))


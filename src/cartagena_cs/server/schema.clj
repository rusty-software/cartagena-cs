(ns cartagena-cs.server.schema
  (:require [schema.core :as s]))

(def Player
  {:uid s/Str
   :name s/Str
   :color s/Keyword
   :cards [s/Keyword]})

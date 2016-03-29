(ns cartagena-cs.server.schema
  (:require [schema.core :as s]))

(def Player
  {(s/optional-key :uid) s/Str
   :name s/Str
   :color s/Keyword
   :cards [s/Keyword]})

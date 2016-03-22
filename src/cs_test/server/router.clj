(ns cs-test.server.router
  (:require
    [taoensso.sente :as sente]
    [taoensso.sente.server-adapters.http-kit :as http-kit]
 ))

(declare channel-socket)

(defn start-websocket []
  (defonce channel-socket
    (sente/make-channel-socket!
      http-kit/sente-web-server-adapter
      {})))




(defn init []
  (println "creating socket...")
  (println "starting router..."))

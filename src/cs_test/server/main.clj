(ns cs-test.server.main
  (:require
    [cs-test.server.router :as router]))

(defn -main [& args]
  (router/init)
  (println "Server starting..."))

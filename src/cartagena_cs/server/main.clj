(ns cartagena-cs.server.main
  (:require
    [cartagena-cs.server.router :as router]
    [environ.core :as environ]
    [org.httpkit.server :as server]
    [taoensso.timbre :as log])
  (:gen-class))

(defn -main [& args]
  (router/init)
  (log/info "Starting server...")
  (server/run-server #'router/handler
                     {:port (or (some-> (first args) (Integer/parseInt))
                                (environ/env :http-port 3000))}))

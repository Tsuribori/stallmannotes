(ns stallmannotes.core
  (:gen-class) 
  (:require [stallmannotes.feed :refer [post-content]]
            [org.httpkit.server :refer [run-server]]))

(defn app [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Start server"})

(defn -main
  []
  (run-server app {:port 8080})
  (try
    (post-content)
    (catch Exception _ (println "Couldn't fetch feed!"))))

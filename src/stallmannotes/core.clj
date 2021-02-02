(ns stallmannotes.core
  (:gen-class) 
  (:require [stallmannotes.feed :refer [post-content]]
            [org.httpkit.server :refer [run-server]]
            [compojure.core :refer [defroutes GET]]))

(defn app [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Start server"})

(defn run [_]
  (try
    (post-content)
    (catch Exception _ (println "Couldn't fetch feed!")))
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Fetch feed"})

(defroutes all-routes
  (GET "/" [] app)
  (GET "/run" [] run))

(defn -main
  []
  (run-server all-routes {:port 8080}))

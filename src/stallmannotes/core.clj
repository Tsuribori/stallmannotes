(ns stallmannotes.core
  (:gen-class)
  (:require [stallmannotes.feed :refer [post-content]]))

(def day (* 1000 60 60 24))

(defn -main
  [] 
  (while true
    (do
      (try
        (post-content)
        (catch Exception _ (println "Couldn't fetch feed!")))
      (Thread/sleep day))))

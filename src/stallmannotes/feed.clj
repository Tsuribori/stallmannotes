(ns stallmannotes.feed
  (:require [feedparser-clj.core :as p]
            [hickory.core :as h]
            [clojure.string :as str]
            [stallmannotes.tweet :as tweet]))


(def url "https://stallman.org/rss/rss.xml")
(defn now [] (.getTime (java.util.Date.)))

(defn get-feed
  []
  (p/parse-feed url))

(defn to-hours
  [milsec]
  (int (/ milsec 1000 60 60)))

(defn get-date
  [seq]
  (.getTime (:published-date seq)))

(defn was-yesterday?
  [seq]
  (<
   (to-hours (- (now) (get-date seq)))
   24))

(defn get-content [coll] (first (:content coll)))

(defn process-node ;; Get node contents
  [node]
  (case (:tag node)
    :a (format "%s (%s)" (get-content node) (get-in node [:attrs :href])) ;; Link
    nil node ;; No node, just text
    (format "%s" (get-content node)))) ;; Anything else, eg em.


(defn split-space [seq] (str/split seq #" "))
(defn replace-whitespace [seq] (str/replace seq #"\s+" " "))
(defn remove-star [seq] (str/replace seq #"\*" "")) ;; Twitter seems to filter all posts with '*' character

(defn strip-whitespace ;; This is very messy
  [seq]
  (->>
   seq
   (clojure.string/join "")
   str/trim
   replace-whitespace
   remove-star
   split-space
   (filter #(not (clojure.string/blank? %))) 
   (clojure.string/join " ")))

(defn process-children
  [seq]
  (loop [new-seq seq processed-seq []]
    (if (empty? new-seq)
      processed-seq
      (recur
       (rest new-seq)
       (conj processed-seq (process-node (first new-seq)))))))

(defn get-html-content
  [seq]
  (->>
   (get-in seq [:description :value])
   h/parse-fragment
   (map #(h/as-hickory %))
   (map #(:content %))
   (map #(process-children %))
   (map #(strip-whitespace %))))


(defn get-yesterdays-content
  []
  (filter was-yesterday?
          (take 25 (:entries (get-feed)))))

(defn get-feed-content
  []
  (map get-html-content (get-yesterdays-content)))

(defn post-content
  []
  (println "Fetching feed...")
  (doall (map tweet/post-to-twitter (get-feed-content))))

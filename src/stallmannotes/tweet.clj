(ns stallmannotes.tweet
  (:require [twitter.oauth :as oauth]
            [twitter.api.restful :as rest]
            [clojure.string :as str]
            [environ.core :refer [env]]))

(def my-creds (oauth/make-oauth-creds (env :consumer-key)
                                      (env :consumer-secret)
                                      (env :access-token)
                                      (env :access-token-secret)))

(defn effective-len
  [paragraph]
  (+
   (apply + (map count paragraph)) ;; char count
   (- (count paragraph) 1))) ;; spaces

(defn tweet-fits?
  [tweet]
  (<= (effective-len tweet) 280))

(defn get-partition-size
  [coll]
  (int (count coll)))

(defn split-into-tweets
  [coll]
  (loop [partition-n (get-partition-size coll)]
    (let [parted-coll (partition partition-n partition-n nil coll)]
      (if (every? tweet-fits? parted-coll)
        parted-coll
        (recur (dec partition-n))))))

(defn to-words-and-back
  [coll]
  (map #(str/join #" " %) (split-into-tweets (str/split coll #" "))))

(defn process-to-tweets
  [coll]
  (flatten (map to-words-and-back coll)))


(defn get-id [response] (:id (:body response)))

(defn make-tweet
  [reply-id tweet]
  (println tweet)
  (Thread/sleep 1000)
  (try
    (if reply-id
      (get-id (rest/statuses-update :oauth-creds my-creds
                                    :params {:status tweet
                                             :in_reply_to_status_id reply-id
                                             :auto_populate_reply_metadata true}))
      (get-id (rest/statuses-update :oauth-creds my-creds
                                    :params {:status tweet})))
    (catch Exception e (println e) nil)))


(defn make-tweet-wrapper
  [id tweet]
  (loop [t-id id t-content tweet tries 3]
    (let [response (make-tweet t-id t-content)]
      (if (or response (not (< 0 tries )))
        response
        (recur t-id t-content (dec tries))))))

(defn reply-cycle
  [coll]
  (loop [id nil tweets-left coll]
    (when (seq tweets-left)
      (recur
       (make-tweet-wrapper id (first tweets-left))
       (rest tweets-left)))))


(defn post-to-twitter
  [coll]
  (reply-cycle (process-to-tweets (seq coll))))

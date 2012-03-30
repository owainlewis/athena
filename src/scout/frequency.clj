(ns scout.frequency
  (:use [clojure.java.io :as io]))

;; Tools for analysing word frequencies in text, map reduce jobs etc

(defn tokenize
  ""
  [line]
  (let [tokens (clojure.string/split (.toLowerCase line) #"\s+")]
    (apply concat
      (reduce (fn [a b]
        (update-in a [b] (fnil inc 0))) {} tokens))))
		   
(defn combine [result]
  (apply concat result))
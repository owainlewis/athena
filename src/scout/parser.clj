(ns scout.parser
  (:use [clojure.java.io :as io]))

;; Tools for analysing text

(defn has-text?
  "Returns true if a expr reg ex matches on candidate text"
  [candidate expr]
  (boolean (re-find expr candidate)))

(defn tokenize
  ""
  [line]
  (let [tokens (clojure.string/split (.toLowerCase line) #"\s+")]
    (apply concat
      (reduce (fn [a b]
        (update-in a [b] (fnil inc 0))) {} tokens))))
		   
(defn combine [result]
  (apply concat result))
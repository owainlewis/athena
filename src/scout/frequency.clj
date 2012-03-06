(ns scout.frequency
  (:use clojure.java.io :as io))

(defn word-freq
  "Create a word frequency map"
  [f]
  (->> f
      io/read-lines
      (mapcat (fn [l] (map #(.toLowerCase %) (re-seq #"\w+" l))))
      (reduce #(assoc %1 %2 (inc (%1 %2 0))) {})
      (sort-by (comp - val))))
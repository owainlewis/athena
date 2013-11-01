(ns athena.examples.nyt
  (:use [athena.core :as ath]))

(def homepage (ath/document "http://www.nytimes.com"))

(def stories (ath/query-selector homepage ".story"))

(defn parse-story
  "Parse a story into a Clojure map"
  [story]
  {:title (ath/text (first (ath/query-selector story "a")))
   :author (ath/text (first (ath/query-selector story ".byline")))
   :summary (ath/text (first (ath/query-selector story ".summary")))})

(defn headlines
  "Returns the latest stories from the NY Times website"
  []
  (map parse-story stories))

(defn get-first-headline-from-nyt 
  "Return the first headline from the homepage of the New York Times"
  []
  (:title (first (headlines))))

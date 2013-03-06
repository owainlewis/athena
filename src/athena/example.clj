(ns athena.example
  (:use [athena.core]))

;; In this example we want to pull in the top headlines from the New York Times
;; and return them as a map

(def homepage (get-document "http://www.nytimes.com"))

(def stories (query-selector homepage ".story"))

(defn parse-story
  "Parse a story into a Clojure map"
  [story]
  {:title (text (query-selector story "a"))
   :author (text (query-selector story ".byline"))
   :summary (text (query-selector story ".summary"))})

(defn headlines
  "Returns the latest stories from the NY Times website"
  []
  (map parse-story stories))

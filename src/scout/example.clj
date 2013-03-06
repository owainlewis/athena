(ns scout.example
  (:use [scout.document :as d]
        [scout.node :as node]))

;; In this example we want to pull in the top headlines from the New York Times
;; and return them as a map

(def homepage (d/get-document "http://www.nytimes.com"))

(def stories (node/query-selector homepage ".story"))

(defn parse-story
  "Parse a story into a Clojure map"
  [story]
  {:title (node/text (node/query-selector story "a"))
   :author (node/text (node/query-selector story ".byline"))
   :summary (node/text (node/query-selector story ".summary"))})

(defn headlines
  "Returns the latest stories from the NY Times website"
  []
  (map parse-story stories))

(ns athena.examples.nyt
  (:require [athena.core :as ath]))

(def homepage (ath/document "http://www.nytimes.com"))

(def page-title (ath/title homepage))
;; "The New York Times - Breaking News, World News & Multimedia"

(def stories (ath/query-selector homepage ".story"))

(defn parse-story
  "Parse a story into a Clojure map"
  [story]
  {:title (ath/text (ath/first-selector story "a"))
   :author (ath/text (ath/first-selector story ".byline"))
   :summary (ath/text (ath/first-selector story ".summary"))})

;; Now we can parse a story

;; (parse-story (first stories))

;; {:title "Presbyterians Vote to Divest Holdings to Pressure Israel",
;;  :author "By LAURIE GOODSTEIN",
;;  :summary "The church voted at its general convention to divest from three
;;            companies that it says supply Israel with equipment used in
;;            the occupation of Palestinian territory."}

(defn headlines
  "Returns the latest stories from the NY Times website"
  []
  (map (parse-story) stories))

(defn get-first-headline-from-nyt
  "Return the first headline from the homepage of the New York Times"
  []
  (:title (first (headlines))))

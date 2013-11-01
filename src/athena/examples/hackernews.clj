(ns athena.examples.hackernews
  (:use [athena.core :as ath]))

;; Crawl the front page of hacker news

;; Because of the HTTP redirects I just downloaded the HTML locally

(def homepage (ath/document "test/fixtures/hacker.html"))

(defn homepage-links 
  "Prints out all the links from the homepage of hacker news
   (which is saved locally as a HTML file)"
  []
  (let [links (->> (ath/query-selector homepage "td.title a") (map text))]
    (doseq [link links]
      (println link))))



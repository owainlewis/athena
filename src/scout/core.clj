(ns scout.core
  (:require [scout.document :as document]
            [scout.node :as node]))

;; A queue for storing links to crawl

(def link-queue (ref clojure.lang.PersistentQueue/EMPTY))

(defn enqueue-link
  "Add a link to the queue"
  [link]
  (dosync
    (alter link-queue conj link)))

(defn pop-link [queue]
  (dosync 
    (let [item (peek @queue)]
      (alter queue pop) item)))

;; URL Processing functions

(defn parse-full-url
  "Trys to convert a partial to a full url. This needs to be more robust
   as we will need to deal with lots of strange url cases i.e params
   strange extensions etc."
  ([url]
     (if (.startsWith url "www")
       (str "http://" url)
       url))
  ([url host]
  (let [protocol "http://"]
  (cond (.startsWith url "/") (str host url)
        (.startsWith url "www") (str protocol url)
        :default host))))

;; Public api

(defn fetch
  "Fetch a document from the web and break it down into parts
  :document :title :head :body :text "
  [url]
  (let [d (document/get-document url)]
    (document/deconstruct d)))

(defn find-nodes
  "Find all nodes in a document matching a selector"
  [document selector]
  (let [nodes (into [] (node/find-by-selector document selector))]
    (map (fn [node]
      (node/parse-element node)) nodes)))

;; Utilities to parse page text

(defn words->seq
  "Pull out a map of unique words from the web page body text (useful for parsing articles)"
  [text]
  (->> (clojure.string/split text #"\W+")]
       (map #(.toLowerCase %))
       (filter #(< 4 (count %))))))


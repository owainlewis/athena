(ns scout.core
  (:use [clojure.string :only [split]])
  (:import [org.jsoup.nodes Document Element])
  (:require [clojure.java.io :as io])
  (:require [scout.document :as document]
            [scout.node :as node]))

;; -----------------------------------
;; A queue for storing links to crawl
;; ----------------------------------

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

;; --------------------
;; Document parser
;; --------------------

(defn get-document
  "Fetch a document from the web"
  [url]
  (try
    (let [response (org.jsoup.Jsoup/connect url)]
      (.get response))
  (catch Exception e
    (print e))))

(defn deconstruct
  "Break a document down into core parts"
  [^org.jsoup.nodes.Document doc]
  (let [t (.title doc)
        h (.head doc)
        b (.body doc)
        txt (.text b)]
   {:title t
    :head h
    :body b
    :text txt}))

(defn fetch [url]
  (let [result (deconstruct (get url))]
    (merge result {:url url})))

;; --------------------
;; IO
;; --------------------

(defprotocol IO
  "A protocol for reading and writing. Used for Scout report output"
  (read-file [this])
  (write-file [a b]))

(extend-protocol IO
  String
  (read-file [file]
    (with-open [rdr (io/reader file)]
      (reduce conj [] (line-seq rdr))))
  (write-file [text dest]
    (with-open [w (io/writer dest)]
      (.write w text))))

;; --------------------
;; Nodes
;; --------------------

(defn to-string [v]
  (if (keyword? v) (name v) v))

(defn query-selector
  "Find all matching elements in a document"
  [document element]
  (let [q (to-string element)]
    (.select document q)))

(defn get-attr
  "Extracts attributes from an element
   can pull out multiple attributes"
  [element & attrs]
  (map
    (fn [attr]
      (let [a (to-string attr)]
        (.attr element a))) attrs))

(defn text [node] (.text node))

(defn images
  "Finds all images"
  [document]
  (query-selector document :img))

(defn links
  "Finds all links with a href attribute"
  [document]
  (query-selector document "a[href]"))

(defn outbound-links
  "Returns a collection of href values for a given document"
  [document]
  (let [all (links document)]
    (map #(get-attr % :href) all)))

;; --------------------
;; Elements parser
;;
;; Convert jSoup nodes to Clojure maps
;; --------------------

(defmulti parse-element (fn [e] (.tagName e)))

(defmethod parse-element "a" [e]
  {:type :link
   :id (.id e)
   :href (.attr e "href")
   :class (.className e)
   :anchor (.ownText e)})

(defmethod parse-element :default [e]
  {:id (.id e)
   :class (.className e)})

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

;; --------------------
;; General Utils
;; --------------------

(defn find-nodes
  "Find all nodes in a document matching a selector"
  [document selector]
  (let [nodes (into [] (node/query-selector document selector))]
    (map (fn [node]
      (node/parse-element node)) nodes)))

(defn words->seq
  "Pull out a map of unique words from the web page body text (useful for parsing articles)"
  [text]
  (->> (split text #"\W+")
       (map #(.toLowerCase %))
       (filter #(< 4 (count %)))))


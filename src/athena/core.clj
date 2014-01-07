(ns athena.core
  (:require [clojure.string :as str])
  (:import [org.jsoup.nodes Document Element]))

(defn parse
  "Takes a raw HTML string and parses into a jSoup document"
  [^String html]
  (org.jsoup.Jsoup/parse html "UTF-8"))

(defn parse-html
  "Read static HTML from directly from a file"
  [path]
  (parse (slurp path)))

(defn get-document
  "Fetch a document from the web"
  [url]
  (.get (org.jsoup.Jsoup/connect url)))

(defn document
  "Fetch a document. If a URL is supplied, Athena will fetch it
   before parsing"
  [path]
  (if (.startsWith path "http://")
    (get-document path)
    (parse-html path)))

;; ************************************************************
;; Nodes
;; ************************************************************

(defn kw-to-string [v]
  (if (keyword? v) (name v) v))

(defn query-selector
  "Find all matching elements in a document"
  [document element]
  (let [q (kw-to-string element)]
    (.select document q)))

(defn ?>>
  "Like query-selector but can accept multiple elements
   allowing for nested searches"
  [document & elements]
  (let [el (into [] elements)]
    (last
      (reduce
      (fn [acc e]
        (if (zero? (count acc))
          (conj acc (query-selector document e))
          (if-let [next ((comp first last) acc)]
            (conj acc (query-selector next e)))))
          [] el))))

(comment
  (?>> (fetch "http://owainlewis.com" :head :meta)))

(defn get-attr
  "Extracts attributes from an element
   can pull out multiple attributes"
  [element & attrs]
  (map
    (fn [attr]
      (let [a (kw-to-string attr)]
        (.attr element a))) attrs))

(def attr (comp first ath/get-attr))

(defn text
  "Extracts text from any node"
  [node]
  (.text node))

(defn imports [doc]
  (query-selector doc "link[href]"))

(defn stylesheets [doc]
  (query-selector doc "link[rel=stylesheet]"))

(defn metadata [doc]
  (query-selector doc "meta"))

(defn data [element]
  (.data element))

;; Document images
;; ************************************************************

(defn get-images
  "Finds all images in a document"
  [^org.jsoup.nodes.Document document]
  (query-selector document :img))

(defn get-image-links
  "Returns all the image links from a document"
  [document]
  (->> document
       get-images
       (mapcat #(get-attr % :src))
       (into [])))

(defmulti images class)

(defmethod images
  java.lang.String
  [url]
  (->> (document url)
        get-images))

;; Document links
;; ************************************************************

(defn get-links
  "Finds all links"
  [document]
  (query-selector document "a"))

(defmulti links class)

(defmethod links
  java.lang.String
  [url]
  ((comp get-links get-document) url))

(defmethod links
  org.jsoup.nodes.Document
  [document]
    (get-links document))

;; Get href values from links

(defn get-hrefs
  "Returns a vector of href values for a given document"
  [document]
  (->> (links document)
       (map #(get-attr % :href))
       (mapcat identity)
       (into [])))

(defn get-absolute-hrefs
  "Same as above except it attempts to resolve absolute paths
   which might be useful if you're writing a crawler"
  [document]
  (->> (links document)
       (map #(get-attr % "abs:href"))
       (mapcat identity)
       (into [])))

(def hrefs-from-url
  (comp get-hrefs get-document))

(defmulti hrefs class)

(defmethod hrefs
  String
  [url]
  (hrefs-from-url url))

(defmethod hrefs
   org.jsoup.nodes.Document
  [document] (get-hrefs document))

;; Utility functions
;; ************************************************************

(defrecord Article [title body])

(defn deconstruct
  "Break a document down into core parts"
  [^org.jsoup.nodes.Document doc]
  (let [title (.title doc)
        body (.text (.body doc))]
  (Article. title body)))

(defn fetch [url]
  (let [result (deconstruct (get-document url))]
    (merge result {:url url})))

;; ************************************************************
;; Elements parser
;;
;; Convert jSoup nodes to Clojure maps
;; ************************************************************

(defmulti parse-element
  (fn [e] (.tagName e)))

(defmethod parse-element "a" [e]
  {:type :link
   :id (.id e)
   :href (.attr e "href")
   :class (.className e)
   :anchor (.ownText e)})

(defmethod parse-element :default [e]
  {:id (.id e)
   :data (.data e)
   :class (.className e)})

;; URL Processing functions
;; ************************************************************

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

;; ************************************************************
;; General Utils
;; ************************************************************

(defn find-nodes
  "Find all nodes in a document matching a selector"
  [document selector]
  (let [nodes (into [] (query-selector document selector))]
    (map (fn [node]
      (parse-element node)) nodes)))

(defn words->seq
  "Pull out a map of unique words from the web page body text (useful for parsing articles)"
  [text]
  (->> (str/split text #"\W+")
       (map #(.toLowerCase %))))

(defn multicrawl [& links]
  (doall
    (map #(future (get-document %))
      (into [] links))))

(defn expose [crawl-result]
  (map deref crawl-result))

;; ************************************************************
;; A queue for storing links to crawl
;; ************************************************************

(def link-queue
  (ref clojure.lang.PersistentQueue/EMPTY))

(defn enqueue-link
  "Add a link to the queue"
  [link]
  (dosync
    (alter link-queue conj link)))

(defn pop-link [queue]
  (dosync
    (let [item (peek @queue)]
      (alter queue pop) item)))

;; ************************************************************

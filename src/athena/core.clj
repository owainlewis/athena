(ns athena.core
  (:require [clojure.string :as str])
  (:import [org.jsoup.nodes Document Element]))

;; -----------------------------------
;; A queue for storing links to crawl
;; ----------------------------------

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

;; --------------------
;; Document parser
;; --------------------

(defn parse [html]
  (org.jsoup.Jsoup/parse html "UTF-8"))
  
(defn parse-html 
  "Read static HTML from disk"
  [path]
  (parse (slurp path)))

(defn get-document
  "Fetch a document from the web"
  [url]
  (try
    (.get (org.jsoup.Jsoup/connect url))
  (catch Exception e
    {:exception e})))

(defn document 
  "Fetch a document. If a URL is supplied, Athena will fetch it
   before parsing"
  [path]
  (if (.startsWith path "http://")
    (get-document path)
    (parse-html path)))

;; --------------------
;; Nodes
;; --------------------

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

(defn text [node] (.text node))

(defn images
  "Finds all images in a document"
  [document]
  (query-selector document :img))

(defn image-links 
  "Returns all the image links from a document"
  [document]
  (->> document images (mapcat #(get-attr % :src)) (into [])))

(defn links
  "Finds all links with a href attribute"
  [document]
  (query-selector document "a[href]"))

(defn outbound-links
  "Returns a collection of href values for a given document"
  [document]
  (let [all (links document)]
    (map #(get-attr % :href) all)))

;; Utility functions

(defn deconstruct
  "Break a document down into core parts"
  [^org.jsoup.nodes.Document doc]
  (let [t (.title doc)
        h (.head doc)
        b (.body doc)]
   {:title t
    :head h
    :body b}))

(defn fetch [url]
  (let [result (deconstruct (get-document url))]
    (merge result {:url url})))

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
  (let [nodes (into [] (query-selector document selector))]
    (map (fn [node]
      (parse-element node)) nodes)))

(defn words->seq
  "Pull out a map of unique words from the web page body text (useful for parsing articles)"
  [text]
  (->> (str/split text #"\W+")
       (map #(.toLowerCase %))))

(defn multicrawl [& links]
  (doall (map #(future (get-document %)) (into [] links))))

(defn expose [crawl-result]
  (map deref crawl-result))

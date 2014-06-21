(ns athena.util
  (:require [athena.core :refer :all]))

;; Misc stuff I've removed from core because it probably isn't needed

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

(ns athena.core
  (:require [clojure.string :as str]
            [clojure.core.async :refer [chan]]
            [org.httpkit.client :as http])
  (:import [org.jsoup.nodes Document Element]
           [java.net URL]))

(defn parse-string
  "Takes a raw HTML string and convert into a jSoup document"
  [^String html]
  (let [encoding "UTF-8"]
    (org.jsoup.Jsoup/parse html encoding)))

(defn parse-file
  "Read static HTML from directly from a file"
  [path]
  ((comp parse-string slurp) path))

(defn url-like?
  "Weak checking for URL strings"
  [url]
  (or (.startsWith url "https://") 
      (.startsWith url "http://")))

(defn jsoup-get-document
  "Given a URL fetch a document from the web and return a document"
  [url]
  (.get (org.jsoup.Jsoup/connect url)))

(defn http-get [url]
  (let [{:keys [_ _ body e] :as resp}
    @(http/get url)]
  (when (nil? e)
    body)))

(defn get-document
  "Make a HTTP request, parse a document and return it"
  [url]
  (let [{:keys [_ _ body e] :as resp} @(http/get url)]
    (when (nil? e)
      (parse-string body))))

(defn get-document-async
  "Fetch the document asynchronously"
  ([url callback-fn]
    (let [options {:timeout 200}]
      (http/get url options
        (fn [{:keys [status headers body error]}]
          (when-not error
            (let [document (parse-string body)]
              (callback-fn document))))))))

(defn document
  "Fetch a document. If a URL is supplied, Athena will fetch it
   before parsing"
  [path]
  (if (url-like? path)
    (get-document path)
    (parse-file path)))

;; ************************************************************
;; Nodes
;; ************************************************************

(defn query-selector
  "Find all matching elements in a document"
  [document element]
  (.select document 
    (name element)))

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

(defn attr
  "Extract an attribute from an element i.e :href :src etc"
  [element attr]
  (.attr element 
    (name attr)))

(defn get-attrs
  "Extracts attributes from an element
   can pull out multiple attributes"
  [element & attrs]
  (map attr 
    (into [] attrs)))

;; Composition functions
;; ************************************************************

(def first-selector
  "Gets only the first matching element"
  (comp first query-selector))

;; General extraction utils

(defn text
  "Extracts text from any node"
  [element]
  (.text element))

(def first-text "Find the text inside the first matching element" (comp text first-selector))

(defn outer-html
  "Extracts the outer HTML from an element"
  [element]
  (.outerHtml element))

(defn html
  "Extract the HTML from an element"
  [element]
  (.html element))

(defn outbound-links
  "Returns all links with a href value set"
  [doc]
  (query-selector doc "link[href]"))

(defn stylesheets
  "Extract the stylesheets from a document"
  [doc]
  (query-selector doc "link[rel=stylesheet]"))

(defn scripts
  "Extract script elements from a page"
  [doc]
  (query-selector doc :script))

(defn title
  "Returns the document title"
  [doc]
    (.title doc))

(defn metadata
  "Extract meta data from a document"
  [doc]
    (query-selector doc :meta))

(defn data
  "Data attributes"
  [element]
    (.data element))

;; TODO !!!

(defn absolute-url [link]
  (.absUrl link "href"))

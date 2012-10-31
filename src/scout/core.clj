(ns scout.core
  (:require [clj-http.client :as client])
  (:use [scout.io :as io])
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Elements]
           [org.jsoup.nodes Element Document]))

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
      (alter queue pop)
        item ;; Return the item
      )))

;; GET URL

(defn get-url
  "A simple get request"
  [url]
  (let [result (client/get url)]
    result))

;; API ideas

;; (defn crawl
;;   {:url "http://www.owainlewis.com"
;;    :onSuccess (fn [x] (println x))})

(defprotocol WebParser
  "Protocol for reading and writing web pages"
  (reader [page])
  (writer [page dest]))

(extend-protocol WebParser
  java.io.File
  (reader [page])
  (writer [page dest]))

(extend-protocol WebParser
  String
  (reader [page]
    (letfn [(uri? [x] (not (nil? (re-find #"^(http|https)://.*" x))))]
      (if (uri? page)
        (.get (Jsoup/connect page))
        (throw (Exception. "Invalid URL")))))
  (writer [page dest]
    (let [output (str (reader page))]
    (io/write-file dest output)))) 

;;; API 

(defn- response [url]
  ((juxt :status :body) (client/get url)))

(defn- check-status
  "Returns the HTTP status code of a url.
   Will return the error if an invalid url is given."
  [url]
  (try
    (let [request (client/get url)]
      (get request :status))
    (catch Exception e e)))

(defn- status-> [s url]
  (= s (check-status url)))

(def status-ok? (fn [url]
  "Returns true is a 200 status is returned"
  (status-> 200 url)))

(def not-found? (fn [url]
  (status-> 404 url)))

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

(defn get-url 
  "Given a url, returns the html from that page"
  [uri-string]
  (.get (Jsoup/connect (parse-full-url uri-string))))

(defn parse [html]
  (Jsoup/parse html))

(defn get-text
  "Extract only the page text from a url"
  [doc]
  (.text doc))

(def body-text (fn [url] (get-text (get-url url))))

(defn get-attr 
  "Returns the attr value of a node"
  ([node attr]
    (.attr node attr))
  ([node attr attr-val]
    (.attr node attr attr-val)))

(defn fetch [doc el]
  (.select doc el))

(defn page-title [url]
  (fetch (reader url)
         "title"))
	      
(defn get-links
  "Extract out all the links from a web page"
  [doc]
  (fetch doc "a"))

(defn parse-attr [attr url]
  (map #(get-attr % attr)
     (get-links (get-url url))))
  
(defn get-page-hrefs
  "Collect all distinct hrefs from a web page"
  [url]
  (distinct (parse-attr "href" url)))

(defn count-all
  "A functon that counts the number of elements on a page"
  [page el]
  (count (fetch page el)))

(defn print-page-hrefs
  "Utility method to print out a list of all the urls on a page"
  [url]
  (doseq [x (get-page-hrefs url)]
    (prn x)))

(defn links->status
  "Maps every link on a page with a status code i.e [http://www.google.com 200]
   this will break if the url isn't the home page so convert any url to a base
   path. We need to filter out duplicate entries as we don't want to waste time
   checking the same url many times. Also need to append a trailing / to urls
   to make sure we don't get protocol exceptions."
  [url]
  (let [links (filter distinct (get-page-hrefs url))]
    (map 
     #(vector
       (parse-full-url % url)
       (check-status (parse-full-url % url))) links)))

(defn broken-links
  "Extracts broken links from a crawl map"
  [url]
  (println "Crawling for broken links...\n")
  (map first
    (filter 
      (fn [x] (not (= 200 (second x)))) (links->status url))))

(defn find-broken-links [uri]
  (let [results (broken-links (links->status uri))]
    (if (empty? results)
      (println "No broken links")
      results)))

;; Utilities to parse page text

(defn words->seq
  "Pull out a map of unique words from the web page body text (useful for parsing articles)"
  [url]
  (let [text (body-text url)
        tokens (clojure.string/split text #"\W+")]
    (->> tokens
         (map #(.toLowerCase %))
         (filter #(< 4 (count %))))))
  

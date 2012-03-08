(ns scout.core
  (:require [clj-http.client :as client])
  (:import  [org.jsoup Jsoup]
            [org.jsoup.select Elements]
            [org.jsoup.nodes Element Document]))

(defprotocol WebParser
  "Protocol for reading and writing web pages"
  (reader [page])
  (writer [page dest]))

(extend-protocol WebParser
  java.io.File
  (reader [page])
  (writer [page dest]))

(extend-protocol WebParser
  Object
  (reader [page]
    (letfn [(uri? [x] (not (nil? (re-find #"^(http|https)://.*" x))))]
      (if (uri? page)
        (.get (Jsoup/connect page))
        (throw (Exception. "Invalid URL")))))
  (writer [page dest]))

(defn response [url]
  ((juxt :status :body) (client/get url)))

(defn check-status
  "Returns the HTTP status code of a url"
  [url]
  (let [request (client/get url)]
    (get request :status)))
     
(defn status-ok? [url]
  (= 200 (check-status url)))

(defn not-found? [url]
  (= 404 (check-status url)))

(defn parse-full-url 
  [host, url]
  (cond (uri? url) url
        (.startsWith url "/") (str host url)
        :else host))

(defn parse [html]
  (Jsoup/parse html))

(defn get-url 
  "Given a url, returns the html from that page"
  [uri-string]
  (when (uri? uri-string)
    (.get (Jsoup/connect uri-string))))

(defn get-text
  "Extract only the page text from a url"
  [url]
  (.text (get-url url)))

(defn get-attr 
  "Returns the attr value of a node"
  ([node attr]
    (.attr node attr))
  ([node attr attr-val]
    (.attr node attr attr-val)))

(defmacro fetch [doc el]
  `(.select ~doc ~el))
	      
(defn get-links
  "Extract out all the links from a web page"
  [doc]
  (fetch doc "a"))

(defmacro parse-attr [attr url]
  `(map #(get-attr % ~attr)
     (get-links (get-url ~url))))
  
(defn get-page-hrefs
  "Collect all distinct hrefs from a web page"
  [url]
  (distinct (parse-attr "href" url)))

(defn links->status
  "Maps every link on a page with a status code i.e [http://www.google.com 200]"
  [url]
  (let [links (get-page-hrefs url)] 
    (map 
      #(vector (parse-full-url url %) 
               (check-status (parse-full-url url %))) links)))

(defn broken-links
  "Extracts broken links from a crawl map"
  [res]
  (println "Crawling for broken links...\n")
  (map first
    (filter 
      (fn [x] (not (= 200 (second x)))) res)))

(defn find-broken-links [uri]
  (let [results (broken-links (links->status uri))]
    (if (empty? results)
      (println "No broken links")
      results)))
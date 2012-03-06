(ns scout.core
  (:require [clj-http.client :as client])
  (:import  [org.jsoup Jsoup]
            [org.jsoup.select Elements]
            [org.jsoup.nodes Element Document]))

;; TODO
;; Currently links are getting parsed multiple times. Partition links before parsing
;; Handle exceptions better

;; Map reduce jobs on text. Count words on page, word frequency on a web page etc
;; Clean up the core file so that it's actually useful. More elegant code
;; Nice API for getting page elements
;; 

(defonce results (atom []))

;;; STATUS CHECKING

(defn check-request
  "Extracts a key from a HTTP response map"
  [url, k]
  (get (client/get url) k))

(defn check-status
  "Returns the HTTP status code of a url"
  [url]
  (check-request url :status))
     
(defn status-ok? [url]
  (= 200 (check-status url)))

(defn not-found? [url]
  (= 404 (check-status url)))

(defn uri?
  "Return true if a valid uri is given"
  ([uri]
   (not 
     (nil? 
       (re-find #"^(http|https|file)://.*" uri)))))

(defn parse-full-url 
  [host, url]
  (cond (uri? url) url
        (.startsWith url "/") (str host url)
        :else host))

;;; Parser

(defn parse [html]
  (Jsoup/parse html))

(defn get-url 
  "Given a url, returns the html from that page"
  [uri-string]
  (when (uri? uri-string)
    (.get (Jsoup/connect uri-string))))

(defmacro fetch 
  "Fetch and download a webpage"
  [page]
  `(get-url ~page))

(defn get-attr 
  "Returns the attr value of a node"
  ([node attr]
    (.attr node attr))
  ([node attr attr-val]
    (.attr node attr attr-val)))

(defn get-links
  "Extract out all the links from a web page"
  [doc]
  (.select doc "a"))

(defn get-images [doc]
  (.select doc "img"))

(defn get-head [doc]
  (.select doc "head"))

(defn get-meta [doc]
  (.select doc "meta"))

(defmacro parse-attr [attr url]
  `(map #(get-attr % ~attr)
     (get-links (get-url ~url))))
  
(defn get-page-hrefs
  "Collect all distinct hrefs from a web page"
  [url]
  (distinct (parse-attr "href" url)))

(defn get-img-src-values
  "Collect all image src values from a web page"
  [url]
  (map 
    #(get-attr % "src") 
      (get-images
        (get-url url))))

(defn links->status
  "Maps every link on a page with a status code i.e [http://www.google.com 200]"
  [url]
  (let [links (get-page-hrefs url)] 
    (map 
      #(vector 
         (parse-full-url url %) 
         (check-status (parse-full-url url %))) links)))

(defn broken-links
  "Extracts broken links from a crawl map"
  [res]
  (println "Crawling for broken links...\n")
  (map first
    (filter 
      (fn [x]
        (not (= 200 (second x)))) res)))

(defn find-broken-links [uri]
  (let [results (broken-links (links->status uri))]
    (if (empty? results)
        (println "No broken links")
        results)))
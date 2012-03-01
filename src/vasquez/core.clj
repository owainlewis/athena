(ns vasquez.core
  (:require [clj-http.client :as client])
  (:import  [org.jsoup Jsoup]
            [org.jsoup.select Elements]
            [org.jsoup.nodes Element Document]))

(defonce results (atom []))

;;; STATUS CHECKING

(defn check-request [url, k]
  "Extracts a key from a HTTP response map"
  (get (client/get url) k))

(defn check-status [url]
  "Returns the HTTP status code of a url"
  (check-request url :status))
     
(defn status-ok? [url]
  (= 200 (check-status url)))

(defn not-found? [url]
  (= 404 (check-status url)))

(defn uri?
  "Return true if a valid uri is given"
  ([uri]
   (not (nil? (re-find #"^(http|https|file)://.*" uri)))))

(defn parse [html]
  (Jsoup/parse html))

(defn get-url [uri-string]
  "Given a url, returns the html from that page"
  (when (uri? uri-string)
    (.get (Jsoup/connect uri-string))))

(defn get-attr 
  "Returns the attr value of a node"
  ([node attr]
    (.attr node attr))
  ([node attr attr-val]
    (.attr node attr attr-val)))
 
(defn get-links [doc]
  "Extract out all the links from a web page"
  (.select doc "a"))

(defn get-images [doc]
  (.select doc "img"))

(defn get-page-hrefs [url]
  "Collect all hrefs from a web page"
  (map #(get-attr % "href") 
    (get-links 
      (get-url url))))

(defn get-img-src-values [url]
  "Collect all image src values from a web page"
  (map #(get-attr % "src") 
    (get-images
      (get-url url))))

(defn parse-full-url 
  [host, url]
  (cond (uri? url) url
        (.startsWith url "/") (str host url)
        :else host))

(defn links->status [url]
  "Maps every link on a page with a status code i.e [http://www.google.com 200]"
  (let [links (get-page-hrefs url)] 
    (map 
      #(vector 
         (parse-full-url url %) 
         (check-status (parse-full-url url %))) links)))

(defn broken-links [res]
  "Extracts broken links from a crawl map"
  (println "Crawling for broken links...\n")
  (map first
    (filter 
      (fn [x]
        (not (= 200 (second x)))) res)))
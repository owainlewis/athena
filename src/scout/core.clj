(ns scout.core
  (:require [clj-http.client :as client])
  (:use [scout.io :as io]
        [scout.parser :as parser])
  (:import  [org.jsoup Jsoup]
            [org.jsoup.select Elements]
            [org.jsoup.nodes Element Document]))

;; We will store visited links in a cache

(def visited-links (atom {}))

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

(defn response [url]
  ((juxt :status :body) (client/get url)))

(defn check-status
  "Returns the HTTP status code of a url.
   Will return the error if an invalid url is given."
  [url]
  (try
    (let [request (client/get url)]
      (get request :status))
    (catch Exception e e)))
     
(defn status-ok? [url]
  "Returns true is a 200 status is returned"
  (= 200 (check-status url)))

(defn not-found? [url]
  (= 404 (check-status url)))

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

(defn contains-text? [url text]
  "Returns true if a url matches the text given"
  (let [page (reader url)
        candidate (get-text page)]
    (parser/has-text? candidate text)))

(defn url-test [file]
  "Get a list of urls from a text file and parse each url
  checking the status returned. Will form the basis of more
  elaborate test case runs. Should use a regex to check the url and 
  should also catch errors when running the tests. Will aim to produce
  readable test results from this test and offer the option to export the test
  results to a flat file." 
  (let [urls (io/read-file file)
        comment-string "#"]
    (map #(vector % (check-status %))
      (filter #(not (clojure.string/blank? %)) 
              (map #(when-not (.startsWith % comment-string) %) urls)))))

(defn url-test-run [file]
  "A utility function that lets QA people quickly test web
   page status results from a text file of URLs"
  (let [urls (io/read-file file)
        comment-string "#"
        results {}]
    (doseq [url urls]
      (if-not (or (clojure.string/blank? url)
                  (.startsWith url comment-string))
        (let [status (check-status url)]
          (prn (format "%s : %s" url
            (if (= 200 status)
               "pass with status 200"
               (str "fail: " status)))))))))

(defn -main [test-file & args]
  "Main method callable through Lein.
   Runs checks on a url file to make sure all urls are returning 200"
  (url-test-run test-file))
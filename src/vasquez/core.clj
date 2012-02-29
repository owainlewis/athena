(ns vasquez.core
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Elements]
           [org.jsoup.nodes Element Document]))

(defn parse [html]
  (Jsoup/parse html))

(defn get-url [url]
  (.get (Jsoup/connect url)))

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
  "Collects all hrefs from a web page"
  (map #(get-attr % "href") 
    (get-links 
      (get-url url))))

(defn get-img-src-values [url]
  "Collects all hrefs from a web page"
  (map #(get-attr % "src") 
    (get-images
      (get-url url))))
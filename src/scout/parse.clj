(ns scout.parse
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Elements]
           [org.jsoup.nodes Element Document]))

;; Move all parse functions into here

(defn parse [html]
  (Jsoup/parse html))

(defn get-text
  "Extract only the page text from a url"
  [doc]
  (.text doc))

(def body-text
  (fn [url] (get-text (get-url url))))

(defn get-attr 
  "Returns the attr value of a node"
  ([node attr]
    (.attr node attr))
  ([node attr attr-val]
    (.attr node attr attr-val)))

(defmacro fetch [doc el]
  `(.select ~doc ~el))

(defn page-title [url]
  (fetch (reader url) "title"))
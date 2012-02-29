(ns vasquez.core
  (:require [clj-http.client :as client])
  (:import [org.jsoup Jsoup]
           [org.jsoup.select Elements]
           [org.jsoup.nodes Element Document]))

(defn get-html [url]
  (first 
    (rest 
      (last (client/get url)))))

(defn parse [html]
  (Jsoup/parse html))

(defn get-url [url]
  (.get (Jsoup/connect url)))

(defn get-links [doc]
  "Extract out all the links from a web page"
  (.select doc "a[href]"))

(defn get-images [doc]
  (.select doc "img[src]"))


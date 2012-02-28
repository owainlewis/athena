(ns vasquez.core
	(:require [clj-http.client :as client])
	(:import [org.jsoup Jsoup]))

(defn get-html [url]
  (first (rest (last (client/get url)))))

(defn parse [s]
  (Jsoup/parse s))

(defn get-url [url]
  (.get (Jsoup/connect url)))

(defn get-links [doc]
  "Extract out all the links from a web page"
  (.select doc "a[href]"))

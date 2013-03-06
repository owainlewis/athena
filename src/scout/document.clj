(ns scout.document
  (refer-clojure :exclude [get])
  (:use [scout.node])
  (:import [org.jsoup.nodes Document]))

(defn get-document
  "Fetch a document from the web"
  [url]
  (try
    (let [response (org.jsoup.Jsoup/connect url)]
      (.get response))
  (catch Exception e
    (print e))))

(defn deconstruct
  "Break a document down into core parts"
  [^org.jsoup.nodes.Document doc]
  (let [t (.title doc)
        h (.head doc)
        b (.body doc)
        txt (.text b)]
   {:title t
    :head h
    :body b
    :text txt}))

(defn fetch [url]
  (let [result (deconstruct (get url))]
    (merge result {:url url})))


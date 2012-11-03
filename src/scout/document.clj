(ns scout.document
  (:import [org.jsoup.nodes Document]))

(defn get-document
  "Fetch a document from the web"
  [url]
  (.get (org.jsoup.Jsoup/connect url)))

(defn deconstruct 
  "Break a document down into separate parts"
  [^org.jsoup.nodes.Document doc]
  (let [t (.title doc)
        h (.head doc)
        b (.body doc)
        txt (.text doc)]
   {:document d
    :title t
    :head h
    :body b
    :text txt}))
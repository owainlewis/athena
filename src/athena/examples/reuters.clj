(ns athena.demos.reuters
  (:require [athena.core :as ath]))

(defrecord Quote [
    price
    change
    percentage-change
    open
    close ])

(defn to-number
  [value]
  (read-string
    (clojure.string/replace value #"," "")))

(defn quote-for
  "Extracts a stock quote for a valid symbol"
  [symbol]
  (let [url (str "http://uk.reuters.com/business/markets/index?symbol=." symbol)
        document (ath/get-document url)]
    (let [[qprice qchange qopen qclose]
           (mapv (comp to-number ath/text)
             (ath/query-selector
               document ".module .dataHeader"))]
      (let [qpercentage (->> (ath/query-selector document ".dataParenthetical.changeDown")
                             first
                             ath/text) ]
        (Quote. qprice qchange (clojure.string/replace qpercentage #"[()]" "") qopen qclose)))))
(defn dow []
   (quote-for "DJI"))

(defn sp500 []
   (quote-for "SPX"))

(defn nasdaq []
  (quote-for "IXIC"))

(defn ftse100 []
  (quote-for "FTSE"))

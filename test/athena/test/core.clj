(ns athena.test.core
  (:require [athena.test.helpers :refer :all])
  (:require [athena.core :as ath])
  (:require [clojure.test :refer :all]))

(deftest url-like-test
  (testing "should return true if a string is URL like"
    (is (ath/url-like? "http://mysite.com"))))

(deftest can-fetch-document-from-web
  (testing "should return a jsoup document from URL"
    (let [document (ath/get-document "http://owainlewis.com")]
      (is (= (class document) org.jsoup.nodes.Document)))))

(deftest can-parse-static-html
  (testing "should read a file from disk"
    (is (= (class (ath/parse-file new-york-times))
                   org.jsoup.nodes.Document))))

(deftest can-parse-html-element-text
  (testing "should parse the html title element and extract the text"
    (let [element (first (ath/query-selector (ath/parse-file new-york-times) :title))]
    (is (= (class element) org.jsoup.nodes.Element))
    (assert (boolean (re-find #"The New York Times" (.text element)))))))

(deftest athena-will-parse-a-html-string
  (testing "the parse function should return a document when passed a HTML string"
    (is (is-document (ath/parse-string "<h1>Hello world!")))))

(deftest document-will-parse-intelligently
  (testing "should fetch url or html depending on context"
    (is (is-document (ath/document "http://owainlewis.com")))))

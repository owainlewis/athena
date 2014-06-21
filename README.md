# Athena

A tool for mining data from web pages and HTML documents.

Athena provides an easy to use DSL for crawling HTML pages and extracting information from them.

It can be used for data-mining, constructing JSON feeds from web pages and more.

Internally Athena uses the JSoup library to process HTML.

## Quick use

Via Clojars

```
[athena "1.0.5"]
```

## Quick example

Find all the inner text from the links on my portfolio

```clojure
(ns myns
  (:require [athena.core :refer :all]))

;; Get a document
(def document (parse-string (http-get "http://owainlewis.com")))

;; Extract all the links from the page
(def links (query-selector document :a))

;; Map over and get the inner text
(map text links)
```

## Documents

The first thing we need to work with is a document. This can be a static file, a html page or a string of html

```clojure
(ns myns
  (:require [athena.core :refer :all]))

(def document-from-string (parse-string "<h1>OH HAI</h1>"))

(text (first-selector document-from-string :h1)) ;; => "OH HAI"

```

## Full example

This is a full example of how you might use this library to turn unstructured web pages into structured clojure data

```clojure

(ns athena.examples.nyt
  (:require [athena.core :as ath]))

(def homepage (ath/document "http://www.nytimes.com"))

(def page-title (ath/title homepage))
;; "The New York Times - Breaking News, World News & Multimedia"

(def stories (ath/query-selector homepage ".story"))

(defn parse-story
  "Parse a story into a Clojure map"
  [story]
  {:title (ath/text (ath/first-selector story "a"))
   :author (ath/text (ath/first-selector story ".byline"))
   :summary (ath/text (ath/first-selector story ".summary"))})

;; Now we can parse a story

;; (parse-story (first stories))

;; {:title "Presbyterians Vote to Divest Holdings to Pressure Israel",
;;  :author "By LAURIE GOODSTEIN",
;;  :summary "The church voted at its general convention to divest from three
;;            companies that it says supply Israel with equipment used in
;;            the occupation of Palestinian territory."}


```

## License

Distributed under the Eclipse Public License, the same as Clojure.

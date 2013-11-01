# Athena

A tool for mining data from web pages and HTML documents.

Athena provides an easy to use DSL for crawling HTML pages and extracting information from them.

It can be used for data-mining, constructing JSON feeds from web pages and more.

Internally Athena uses the JSoup library to process HTML.

## Quick use

Via Clojars

```
[athena "1.0.1-SNAPSHOT"]
```

## Fetch a document

The first thing we need to do is a parse a HTML document into a format we can manipulate.

```clojure

(ns myns
  (:use [athena.core :as ath]))

;; To pass a string of HTML and convert it to a document

(ath/parse "<h1>My HTML</h1>")

;; The document function takes a URL or path to a static
;; HTML file and turns it into document

;; Fetch a document from the internet
(ath/document "http://owainlewis.com")

;; Or a static file
(ath/document "test/fixtures/nyt.html")

```

## Finding elements

You can use query-selector to find elements quickly

```clojure

(def document (ath/document "http://owainlewis.com"))

(def page-title (ath/query-selector document :title))

(def page-headings (ath/query-selector document "h1"))

```

Use the text function to extract text from an element

```clojure

(def element
  (ath/query-selector
    (ath/parse "<h1>hello</h1>") "h1"))

(text element) ;; => "hello"
```

You can also use CSS type queries to find elements

```clojure

(def page (ath/document "http://owainlewis.com"))

(def hero-banner
  (ath/query-selector page "#hero"))
```

## Fetching attributes from elements

You can pull out any number of elements from a node

```clojure

;; Here we want to extract the src and width attribute from an image

(def document (ath/get-document "http://www.yahoo.com"))

(def image (first (ath/images document)))

(ath/get-attr image :src :width)

```

## A complete example

```clojure
(ns athena.example
  (:use [athena.core]))

;; In this example we want to pull in the top headlines from the New York Times
;; and return them as a map

(def homepage (document "http://www.nytimes.com"))

(def stories (query-selector homepage ".story"))

(defn parse-story
  "Parse a story into a Clojure map"
  [story]
  {:title (text (query-selector story "a"))
   :author (text (query-selector story ".byline"))
   :summary (text (query-selector story ".summary"))})

(defn headlines
  "Returns the latest stories from the NY Times website"
  []
  (map parse-story stories))

;; (first (headlines)) =>
;; {:title  "Syrian Rebels Abduct 20 U.N. Soldiers in the Golan Heights Number of Syrian
;;           Refugees Hits 1 Million",
;; :author  "By RICK GLADSTONE and ALAN COWELL",
;; :summary "Syria’s civil war entangled the peacekeeping operation in the disputed
;;           Golan Heights area Wednesday, when 30 armed fighters for the insurgency
;;           detained a group of peacekeepers."}


```

## Helper functions

### All images from a document

```clojure

(def d (ath/document "http://yahoo.com"))

(ath/images d)
```

### All links from a document

```clojure

(ath/links d)
```

### All href values (outgoing links)

```clojure

(ath/outbound-links d)
```

## License

Distributed under the Eclipse Public License, the same as Clojure.

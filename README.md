# Athena

A tool for mining data from web pages and HTML documents.

Athena provides an easy to use DSL for crawling HTML pages and extracting information from them.

It can be used for data-mining, constructing JSON feeds from web pages and more.

Internally Athena uses the JSoup library to process HTML.

## Quick use

Via Clojars

```
[athena "1.0.3"]
```

## Fetch a document

The first thing we need to do is a parse a HTML document into a format we can manipulate.

```clojure

(ns myns
  (:require [athena.core :as ath]))

;; To pass a string of HTML and convert it to a document

(ath/parse "<h1>My HTML</h1>")

;; The document function takes a URL or path to a static
;; HTML file and turns it into document

;; Fetch a document from the internet
(ath/document "http://owainlewis.com")

;; Or a static file
(ath/document "test/fixtures/nyt.html")

```

## Return document text

You can return all the text from a document or element with the text function

```clojure

;; Get the entire document text

(ath/text (ath/document "http://owainlewis.com"))

;; Get the text for a single element

(def link (first (ath/links "http://owainlewis.com")))

(text link) ;; => "Github"

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

(def document (ath/document "http://www.yahoo.com"))

(def image (first (ath/images document)))

(ath/get-attr image :src :width)

```

## Finding links

Extracting links is a fairly common thing you might need and so there is a helper for that.

```clojure
;; get all links from a web page

(ath/links "http://owainlewis.com")

;; The links function is polymorphic and will also parse links from a document

(def document (ath/document "http://owainlewis.com"))

(ath/links document)
```

## A complete example

In this example we want to pull in the top headlines from the New York Times
and return them as a map. This is how you might use this library for
processing web documents.

```clojure

(ns athena.example
  (:use [athena.core :as ath]))

(def homepage (ath/document "http://www.nytimes.com"))

(def stories (ath/query-selector homepage ".story"))

(defn parse-story
  "Parse a story into a Clojure map"
  [story]
  {:title (ath/text (first (ath/query-selector story "a")))
   :author (ath/text (first (ath/query-selector story ".byline")))
   :summary (ath/text (first (ath/query-selector story ".summary")))})

(defn headlines
  "Returns the latest stories from the NY Times website"
  []
  (map parse-story stories))

(defn get-first-headline-from-nyt 
  "Return the first headline from the homepage of the New York Times"
  []
  (:title (first (headlines))))

;; (get-first-headline-from-nyt) =>
;; "Troubled Start for Health Law Has Democrats Anxious
```

## Parse the front page of Hacker News

```clojure
(ns athena.examples.hackernews
  (:use [athena.core :as ath]))

;; Crawl the front page of hacker news

;; Because of the HTTP redirects I just downloaded the HTML locally

(def homepage (ath/document "test/fixtures/hacker.html"))

(defn homepage-links 
  "Prints out all the links from the homepage of hacker news
   (which is saved locally as a HTML file)"
  []
  (let [links (->> (ath/query-selector homepage "td.title a") (map text))]
    (doseq [link links]
      (println link))))
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

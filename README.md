# Scout

A tool for mining data from web pages and HTML documents.

Scout provides an easy to use DSL for crawling HTML pages and extracting information from them.

It can be used for data-mining, constructing JSON feeds from web pages and more.

Internally Scout uses the JSoup library to process HTML.

## A complete example

```clojure
(ns scout.example
  (:use [scout.document :as d]
        [scout.node :as node]))

;; In this example we want to pull in the top headlines from the New York Times
;; and return them as a map

(def homepage (d/get-document "http://www.nytimes.com"))

(def stories (node/query-selector homepage ".story"))

(defn parse-story
  "Parse a story into a Clojure map"
  [story]
  {:title (.text (node/query-selector story "a"))
   :author (.text (node/query-selector story ".byline"))
   :summary (.text (node/query-selector story ".summary"))})

(defn headlines
  "Returns the latest stories from the NY Times website"
  []
  (map parse-story stories))

```

## Use

```clojure

(use [scout.document :as document])

(def doc (document/get-document "http://www.owainlewis.com"))

```
## Finding elements

You can use query-selector to find elements quickly

```clojure

(def page-title (query-selector doc :title))

```

You can also use CSS type queries to find elements

```clojure

(:use [scout.document])

(def page (document/get-document "http://owainlewis.com"))

(def hero-banner (query-selector page "#hero"))
```

## Fetching attributes from elements

You can pull out any number of elements from a node

```clojure
;; Here we want to extract the src and width attribute from an image

(def document (get-document "http://www.yahoo.com"))

(def image (first (images document)))

(get-attr image :src :width)
```

## Helper functions

### All images from a document

```clojure

(def d (get-document "http://yahoo.com"))

(images d)
```

### All links from a document

```clojure
(links d)
```

### All href values (outgoing links)

```clojure
(outbound-links d)
```

## Extraction and Data Mining

We can pull out individual attributes from the page. The following example
will pull out all link elements from the page and return a sequence of maps

```clojure

;; TODO

```

## License

Distributed under the Eclipse Public License, the same as Clojure.

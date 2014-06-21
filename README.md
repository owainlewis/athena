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

## Advanced

This is a more complex example that involves fetching data from multiple pages
to produce the result

We will parse job adverts from multiple pages of Facebook.com

```
{:title "Data Scientist, Economics Research",
 :link "https://en-gb.facebook.com/careers/department?dept=engineering&req=a0IA000000CzAekMAF",
 :description "Facebook was built to help people connect and share, and over the last decade our
               tools have played a critical part in changing how people around the world communicate
			   with one another.
			   With over a billion people using the service and more than fifty offices around the globe,
		       a career at Facebook offers countless ways to make an
			   impact in a fast growing organization.",
  :location "Menlo Park"}

```

```clojure
(ns athena.examples.facebook
  (:require [athena.core :refer :all]))

(def fb-eng "https://en-gb.facebook.com/careers/teams/engineering")

;; Fetch the first reference document which lists all jobs

(def doc (->> fb-eng http-get parse-string))

(defn jobs []
  (lazy-seq (query-selector doc ".careersPositionGroup li")))

(defn normalize-location [location]
  (clojure.string/replace location #"[(]|[)]" ""))

(defn parse-job-detail [href]
  (try
    (let [document (parse-string (http-get href))]
      (text (first-selector document ".mvl .mbl")))
  (catch Exception e (.getMessage e))))

(defn parse-job
  "Returns a job"
  [element]
  (let [atag (first-selector element :a)
        location (text (first-selector element ".fcg"))
        link-href (attr atag :href)
        link-text (text atag)
        full-link (str "https://en-gb.facebook.com" link-href)]
  {:title link-text
   :link  full-link
   :description (parse-job-detail full-link)
   :location (normalize-location location)}))


```

## License

Distributed under the Eclipse Public License, the same as Clojure.

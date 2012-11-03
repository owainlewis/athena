# Scout

A tool for mining data from web pages. Provides a simple wrapper over the JSoup library.

## Use

```clojure

(use [scout.core :as scout])

(def page (scout/fetch "http://www.owainlewis.com"))

;; Get the page title

(:title page)

;; Get the head of the document

(:head page)

;; Get the document text only (for analysis)

(:text page)

;; We can pull out individual attributes from the page. The following example
;; will pull out all link elements from the page and return a sequence of maps

(scout/find-nodes (:document page) "a")

;; => [{:id "home" :class "" :href "/" :anchor "click me"}]

```

## License

Distributed under the Eclipse Public License, the same as Clojure.

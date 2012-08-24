# Scout

A tool for mining data from web pages.

## Usage

```clojure
(use 'scout.core)
```

Fetching a web page is easy

```clojure
(reader "http://www.google.com")
```

Once you have a web page it's easy to extract out elements from the page using fetch. I.e

```clojure
(def page (reader "http://www.owainlewis.com"))
```

Fetch all the links

```clojure
(fetch page "a")
```

Fetch the head of a document

```clojure
(fetch page "head")
```
Fetch all the meta data on the 

```clojure
(fetch page "meta")
```

Fetch the title of a document

```clojure
(fetch (get-url "http://www.owainlewis.com") "title")
```

## Selecting page text

You may want to grab only the actual text from a web page for processing

```clojure
(get-text (get-url "http://www.google.com"))
```

## License

Distributed under the Eclipse Public License, the same as Clojure.

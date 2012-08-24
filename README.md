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

Once you have a web page it's easy to extract out elements from the page using fetch. 

Lets first define a page.

```clojure
(defonce page (reader "http://www.owainlewis.com"))
```

Now we can fetch all the links

```clojure
(fetch page "a")
```

We can fetch the head of a document

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

## Demos

Let's try and pull out a sequence of words from a wikipedia article and remove any words less than 4 chars in length

```clojure
(defn words->seq
  "Pull out a map of unique words from the web page body text (useful for parsing articles)"
  [url]
  (let [text (body-text url)
        tokens (clojure.string/split text #"\W+")]
    (->> tokens
         (map #(.toLowerCase %))
         (filter #(< 4 (count %))))))

(words->seq "http://en.wikipedia.org/wiki/Levenshtein_distance")

```

## License

Distributed under the Eclipse Public License, the same as Clojure.

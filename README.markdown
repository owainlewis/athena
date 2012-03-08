# Scout

Scout is a web crawler written in Clojure. It's used for mining interesting data from web pages.

## Usage

    (use 'scout.core)

Fetching a web page is easy

    (reader "http://www.google.com")

Once you have a web page it's easy to extract out elements from the page using fetch. I.e

    (def page (reader "http://www.owainlewis.com"))
     
Fetch all the links

    (fetch page "a")

Fetch the head of a document
(
    (fetch page "head")

Fetch all the meta data on the page

    (fetch page "meta")

## Selecting page text

You may want to grab only the actual text from a web page for processing

    (get-text "http://www.google.com")

## License

Distributed under the Eclipse Public License, the same as Clojure.

# Scout

Scout is a web crawler written in Clojure. It's used for mining interesting data from web pages.

## Usage

    (use 'scout.core)

Fetching a web page is easy

    (fetch "http://www.google.com")

Creating a map of all page links and status codes on a web page

    (links->status "http://www.mysite.com")

## License

Distributed under the Eclipse Public License, the same as Clojure.

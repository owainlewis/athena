# vasquez

Vasquez is a web crawler written in Clojure. It can be used for grabbing data from web pages, parsing broken links and checking for page errors.

## Usage

    (use 'vasquez.core)

Creating a map of all page links and status codes on a web page

    (links->status "http://www.mysite.com")

## License

Distributed under the Eclipse Public License, the same as Clojure.

# Scout

Scout is a work in progress automated test tool written in Clojure.

It's primary use is for crawling web pages and remote automated testing. 

Ultimately, the aim of the library will be to make it really easy to test web pages for errors.

## Usage

    (use 'scout.core)

Fetching a web page is easy

    (reader "http://www.google.com")

Once you have a web page it's easy to extract out elements from the page using fetch. I.e

    (def page (reader "http://www.owainlewis.com"))
     
Fetch all the links

    (fetch page "a")

Fetch the head of a document

    (fetch page "head")

Fetch all the meta data on the page

    (fetch page "meta")

## Testing URLs

You can easily test large numbers of URLs for errors by creating a text file with a list of urls

create a new file called urls.txt. You can add comments by starting any line with a hash #

    # My websites to test

    http://www.google.com
    http://www.owainlewis.com
    http://www.boxuk.com  
	
Then run a test to make sure every url is returning a 200 status

    (url-test "urls.txt")

This function returns a map of status code and url

    ([200 "http://www.google.com"] [200 "http://www.owainlewis.com"] [200 "http://www.boxuk.com"])
	
You can get easier to read output by running the test in the terminal. Assuming you have a list of urls in urls.txt run:

    lein run "urls.txt"

This will return test output to the terminal.

## Selecting page text

You may want to grab only the actual text from a web page for processing

    (get-text "http://www.google.com")

## License

Distributed under the Eclipse Public License, the same as Clojure.

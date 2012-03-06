(ns scout.report
  (:use scout.core))

;; Reporter that logs the results of a website crawl to file

(defn write-file [filename, data]
  (spit filename data))

(defn page-report [url]
  (let [document (fetch url)
        link-count (count (get-links document))]
    (write-file "report.txt" (str "There are : " link-count " links on " url))))
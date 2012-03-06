(ns scout.report)

;; Reporter that logs the results of a website crawl to file

(defn write-file [filename, data]
  (spit filename data))
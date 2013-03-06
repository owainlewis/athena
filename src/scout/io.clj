(ns scout.io
  (:require [clojure.java.io :as io]))

(defprotocol IO
  "A protocol for reading and writing. Used for Scout report output"
  (read-file [this])
  (write-file [a b]))

(extend-protocol IO
  String
  (read-file [file]
    (with-open [rdr (io/reader file)]
      (reduce conj [] (line-seq rdr))))
  (write-file [text dest]
    (with-open [w (io/writer dest)]
      (.write w text))))

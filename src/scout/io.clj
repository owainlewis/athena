(ns scout.io)

(defprotocol IO
  "A protocol for reading and writing. Used for Scout report output"
  (read-file [a])
  (write-file [a b]))

(extend-protocol IO
  String
  (read-file [file]
    (with-open [rdr (clojure.java.io/reader file)]
      (reduce conj [] (line-seq rdr))))
  (write-file [text dest]
    (with-open [w (clojure.java.io/writer dest)]
      (.write w text))))

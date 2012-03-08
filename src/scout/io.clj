(ns scout.io)

(defn write-file [filename, data]
  (spit filename data))

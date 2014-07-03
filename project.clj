(defproject athena "1.0.6"
  :description "A web crawler written in Clojure"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.16"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.jsoup/jsoup "1.7.3"]]
  :main athena.core)

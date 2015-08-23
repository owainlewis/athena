(defproject athena "1.0.6"
  :description "A web crawler written in Clojure"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.16"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.jsoup/jsoup "1.8.2"]]
  :main athena.core)

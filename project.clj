(defproject scout "1.0.0-SNAPSHOT"
  :description "A web crawler written in Clojure"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [midje "1.3.1"]
                 [lein-midje "1.0.9"]
                 [org.jsoup/jsoup "1.6.1"]
                 [clj-http "0.3.2"]]
  :main scout.core)
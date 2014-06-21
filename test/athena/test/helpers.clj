(ns athena.test.helpers)

(def new-york-times "test/fixtures/nyt.html")

(defn is-document [d]
  (= (class d) org.jsoup.nodes.Document))

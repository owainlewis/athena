(ns scout.node
  (:import [org.jsoup.nodes Element]))

(defn to-string [v]
  (if (keyword? v) (name v) v))

(defn query-selector
  "Find all matching elements in a document"
  [document element]
  (let [q (to-string element)]
    (.select document q)))

(defn get-attr
  "Extracts attributes from an element
   can pull out multiple attributes"
  [element & attrs]
  (map
    (fn [attr]
      (let [a (to-string attr)]
        (.attr element a))) attrs))

(defn text [node] (.text node))

(defn images
  "Finds all images"
  [document]
  (query-selector document :img))

(defn links
  "Finds all links with a href attribute"
  [document]
  (query-selector document "a[href]"))

(defn outbound-links
  "Returns a collection of href values for a given document"
  [document]
  (let [all (links document)]
    (map #(get-attr % :href) all)))

;; Elements parser
;;
;; Convert jSoup nodes to Clojure maps

(defmulti parse-element (fn [e] (.tagName e)))

(defmethod parse-element "a" [e]
  {:type :link
   :id (.id e)
   :href (.attr e "href")
   :class (.className e)
   :anchor (.ownText e)})

(defmethod parse-element :default [e]
  {:id (.id e)
   :class (.className e)})


(ns scout.node
  (:import [org.jsoup.nodes Element]))

(defn find-by-selector
  "Find all matching elements in a document"
  [document element]
  (.select document element))

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
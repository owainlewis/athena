(ns athena.demos.basic
  (:require [athena.core :refer :all]))

(def document-from-string (parse-string "<h1>OH HAI</h1>"))

(def oh-hai
  (text (first-selector document-from-string :h1))) ;; => "OH HAI"

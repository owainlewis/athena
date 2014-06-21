(ns athena.examples.facebook
  (:require [athena.core :refer :all]))

(def fb-eng "https://en-gb.facebook.com/careers/teams/engineering")

(def doc (->> fb-eng http-get parse-string))

(defn jobs []
  (lazy-seq (query-selector doc ".careersPositionGroup li")))

(defn normalize-location [location]
  (clojure.string/replace location #"[(]|[)]" ""))

(defn parse-job-detail [href]
  (try
    (let [document (parse-string (http-get href))]
      (text (first-selector document ".mvl .mbl")))
  (catch Exception e (.getMessage e))))

(defn parse-job
  "Returns a job"
  [element]
  (let [atag (first-selector element :a)
        location (text (first-selector element ".fcg"))
        link-href (attr atag :href)
        link-text (text atag)
        full-link (str "https://en-gb.facebook.com" link-href)]
  {:title link-text
   :link  full-link
   :description (parse-job-detail full-link)
   :location (normalize-location location)}))

(ns vasquez.test.core
  (:use [vasquez.core])
  (:use [clojure.test]))

(deftest uri
  (is (= true (uri? "https://mysite.com")))
  (is (= false (uri? "owainlewis.com"))))
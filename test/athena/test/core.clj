(ns athena.test.core
  (:use [athena.core])
  (:use [clojure.test]))

(deftest can-parse-static-html
  (testing "should read a file from disk"
    (is (= "testing" 12))))


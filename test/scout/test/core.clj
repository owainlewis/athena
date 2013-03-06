(ns scout.test.core
  (:use [scout.core])
  (:use [clojure.test]))

(deftest to-string-test
  (testing "should convert keywords to strings"
    (is (= "test" (to-string :test))))
  (testing "should retain string values"
    (is (= "test" (to-string "test")))))


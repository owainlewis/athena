(ns scout.test.node
  (:use [scout.node])
  (:use [clojure.test]))

(deftest to-string-test
  (testing "should convert keywords to strings"
    (is (= "test" (to-string :test))))
  (testing "should retain string values"
    (is (= "test" (to-string "test")))))

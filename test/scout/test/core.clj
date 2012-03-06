(ns scout.test.core
  (:use [scout.core])
  (:use [midje.sweet])
  (:use [clojure.test]))

(fact (+ 1 1) => 2)
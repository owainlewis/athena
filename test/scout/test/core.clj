(ns scout.test.core
  (:use [scout.core :as scout])
  (:use [midje.sweet])
  (:use [clojure.test]))

(facts "about parsing documents"
  (scout/fetch ""))

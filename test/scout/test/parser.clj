(ns scout.test.parser
  (:use [scout.parser]
        [midje.sweet]
        [clojure.test]))

(def sample-text "This is a string of sample text")

(facts "about finding words"
  (has-text? sample-text #"string of") => true
  (has-text? sample-text #"android") => false)
  
 
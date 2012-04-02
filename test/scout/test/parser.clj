(ns scout.test.parser
  (:use [scout.core]
        [midje.sweet]
        [clojure.test]))

(def sample-text "This is a string of sample text")

(facts "about finding words"
  (has-text? sample-text "string of") => true)
  
 
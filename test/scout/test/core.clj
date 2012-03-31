(ns scout.test.core
  (:use [scout.core])
  (:use [midje.sweet])
  (:use [clojure.test]))

(facts "about url parsing"
  (parse-full-url "www.owainlewis.com") => "http://www.owainlewis.com"
  (parse-full-url "http://www.owainlewis.com") => "http://www.owainlewis.com"
  (parse-full-url "/careers/overview/" "http://www.boxuk.com") => "http://www.boxuk.com/careers/overview/") 

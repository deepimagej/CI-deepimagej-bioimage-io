(ns summaries.errors-test
  (:require [summaries.errors :refer :all]
            [clojure.test :refer :all]))

(deftest find-stage-test
  (is (= (find-stage :no-compatible-weights) :initial))
  (is (= (find-stage :comparison) :reproduce))
  (is (= (find-stage :other-error) :initial)))

(ns utils-test
  (:require [utils :refer :all]
            [clojure.test :refer :all]))

(deftest count-dict-test
  (is (= (count-dict {:a [1 2] :b [4 5 6]}) {:a 2 :b 3})))
(ns reproduce.communicate-test
  (:require [reproduce.communicate :refer :all]
            [clojure.test :refer :all]))

(deftest format-axes-test
  (is (= (format-axes "byxzc") "Y,X,Z,C")))

(deftest format-tiles-test
  (is (= (format-tiles "1 x 256 x 256 x 8 x 1") "256,256,8,1")))

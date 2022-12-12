(ns collection-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]])
  (:require [collection :refer :all]))

(deftest -main-test
  (is (= '("a" "1" 2) (-main "a" "1" 2))))

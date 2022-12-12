(ns collection-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]])
  (:require [collection :refer :all]))

(deftest -main-test
  (is (= '("a" "1" 2) (-main "a" "1" 2))))

(deftest json->vector-test
  (testing "Input is a raw json string"
    (let [str-json "{\"include\": [{\"resource_id\": \"**\", \"version_id\": \"**\"}]}"
          parsed (json->vector str-json)]
      (is (= (count parsed)) 1)
      (is (= (:resource_id (nth (json->vector str-json) 0)) "**"))))
  )

(ns downloads.initial-checks-test
  (:require [downloads.initial-checks :refer :all]
            [test-setup :refer [load-test-paths load-model-records model-records all-model-records]]
            [clojure.test :refer :all]))


(use-fixtures :once load-test-paths load-model-records)

(defn count-dict
  "counts dictionary entries that are seqs"
  [d] (map (fn [[k v]] {k (count v)}) d))

(deftest count-dict-test
  (is (= (count-dict {:a [1 2] :b [4 5 6]}) [{:a 2} {:b 3}])))

(deftest separate-by-dij-config-test
  (testing "The 3 models tf, pt and state-dict (incompatible with dij)"
    (let [separated (separate-by-dij-config @model-records)]
      (is (= (keys separated) [:keep-testing :no-dij-config]))
      (is (= 2 (count (:keep-testing separated))))
      (is (= 1 (count (:no-dij-config separated))))))
  (testing "All models in the collection"
    (let [separated (separate-by-dij-config @all-model-records)]
      (is (= [{:keep-testing 47} {:no-dij-config 117}] (count-dict separated))))))


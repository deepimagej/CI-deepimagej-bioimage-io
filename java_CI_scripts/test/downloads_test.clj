(ns downloads-test
  (:require [downloads :refer :all]
            [models :refer [build-model]]
            [collection :refer [file-json->vector get-rdfs-to-test]]
            [test-setup :refer [rdf-paths load-test-paths]]
            [clojure.test :refer [deftest is testing use-fixtures run-tests]]))

(def model-records (atom nil))
(def all-model-records (atom nil))

(defn load-model-records
  [test-fn]
  (let [all-rdfs (file-json->vector "pending_matrix/all_models.json")]
    (reset! model-records (map (fn [[k v]] (build-model @v)) rdf-paths))
    (reset! all-model-records (map  #(build-model %) (get-rdfs-to-test all-rdfs))))
  (test-fn))

(defn count-dict
  "counts a dictionary entries that are seqs"
  [d] (map (fn [[k v]] {k (count v)}) d))

(use-fixtures :once load-test-paths load-model-records)

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

(ns summaries.init-checks-test
  (:require [summaries.init-checks :refer :all]
            utils
            [test-setup :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :once load-test-paths load-rdfs-parsed load-model-records)

(deftest model?-test
  (is (model? (first @model-records)))
  (is (>= (count (filter model? @all-model-records)) 70)))

(deftest dij-config?-test
  (is (= (count (filter dij-config? @model-records)) 2))
  (is (>= (count (filter dij-config? @all-model-records)) 48)))

(deftest no-run-mode?-test
  (let [{without-rm true with-rm false} (group-by no-run-mode? @all-model-records)]
    (is (> (count without-rm) 161))
    (is (= 4 (count with-rm)))
    (is (= (repeat 4 "deepimagej")
           (mapv #(get-in % [:rdf-info :run-mode :name]) with-rm)))))

(deftest any-compatible-weight?-test
  (is (not (any-compatible-weight? (first @model-records))))
  (is (any-compatible-weight? (second @model-records)))
  (is (any-compatible-weight? (last @model-records)))
  (testing "all models in collection with compatible weights"
    (is (> (count (filter any-compatible-weight? @all-model-records)) 46))))

(deftest available-sample-images?-test
  (is (= 2 (count (filter available-sample-images? @model-records))))
  (is (= 29 (count (filter available-sample-images? @all-model-records)))))

(deftest p*process-in-attachment?-test
  (is (not (p*process-in-attachment?
             (assoc-in (last @model-records) [:rdf-info :attach] []))))
  (is (p*process-in-attachment? (second @model-records))))

(deftest errors-fns-test
  (let [initial-error? (partial contains? (set (keys summaries.errors/initial-errors)))]
    (testing "errors correspond to the same ones in summaries.errors"
      (is (->> (keys errors-fns) (map initial-error?) and)))))
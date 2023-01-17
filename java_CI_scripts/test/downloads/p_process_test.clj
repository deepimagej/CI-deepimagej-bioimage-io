(ns downloads.p-process-test
  (:require [downloads.p-process :refer :all]
            [test-setup :refer [load-test-paths load-model-records an-edn model-records]]
            [clojure.test :refer :all]))

(use-fixtures :once load-test-paths load-model-records)

(deftest get-p*process-names-test
  (let [pp-scripts (map get-p*process-names @model-records)]
    (is (empty? (first pp-scripts)))
    (is (= (second pp-scripts) ["binarize.ijm" "per_sample_scale_range.ijm"]))
    (is (= (nth pp-scripts 2) ["zero_mean_unit_variance.ijm"]))))

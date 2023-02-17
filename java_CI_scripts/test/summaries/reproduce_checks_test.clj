(ns summaries.reproduce-checks-test
  (:require [summaries.reproduce-checks :refer :all]
            [test-setup :refer :all]
            [clojure.test :refer :all]
            [clojure.pprint :as ppr]
            [babashka.fs :as fs]))

(def fake-maps "content of the fake output_metrics.edn"
  [{} {:mse 24422.45962, :mae 96.41069 :max_val 0.99739} {:mse 0.45827, :mae 0.77296 :max_val 1}])

(defn write-fake-outputs
  "write some fake output_metrics.edn for testing"
  [test-fn]
  (mapv (fn [dict model]
          (let [metrics-file (get-metrics-file model)]
            (fs/create-dirs (fs/parent metrics-file))
            (spit metrics-file (with-out-str (ppr/pprint dict)))))
        fake-maps @model-records)
  (test-fn))

(use-fixtures :once load-test-paths load-rdfs-parsed load-model-records write-fake-outputs)

(deftest get-output-metrics-test
  (is (= (get-output-metrics (first @model-records)) {}))
  (is (= (get-output-metrics (second @model-records)) (second fake-maps)))
  (is (= (get-output-metrics (last @model-records)) (last fake-maps))))

(deftest metrics-produced?-test
  (is (not (true? (metrics-produced? (first @model-records)))))
  (is (metrics-produced? (last @model-records))))

(deftest ok-metrics?-test
  (is (not (ok-metrics? (second @model-records))))
  (is (ok-metrics? (last @model-records))))

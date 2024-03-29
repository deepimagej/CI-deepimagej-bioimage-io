(ns summaries.summary-test
  (:require [config :refer [ROOTS]]
            [summaries.init-checks :as init-checks]
            [summaries.summary :refer :all]
            [summaries.errors :as errors]
            [summaries.discriminate :as discriminate]
            [test-setup :refer :all]
            [clojure.test :refer :all]
            [clj-yaml.core :as yaml]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths load-rdfs-parsed load-model-records)

(deftest gen-summa-path-test
  (let [expected-path (fs/path (:summa-root ROOTS) "10.5281" "zenodo.6334881" "6346477")]
    (is (= (str (gen-summa-path @(:an-rdf rdf-paths))) (str expected-path)))))

(deftest create-summa-dir-test
  (let [toy-root (fs/path "../toy_test_summaries")]
    (testing "Before folder exists"
      (is (not (fs/exists? toy-root))))
    (testing "Create folder structure"
      (let [summa-path (gen-summa-path (:collection-root ROOTS) toy-root @(:an-rdf rdf-paths))
            result (create-summa-dir (:collection-root ROOTS) toy-root @(:an-rdf rdf-paths))]
        (is (= (str (fs/absolutize summa-path)) (str (fs/absolutize result)))
            "successful creation returns the path")
        (is (= (map fs/file-name (fs/list-dir toy-root)) ["10.5281"]))))
    (testing "After tests, delete toy folder"
      (let [del (fs/delete-tree toy-root)]
        (is (= (str toy-root) (str del)) "successful delete returns the path")))))

(deftest gen-summa-dict-test
  (is (= (gen-summa-dict :no-dij-config)
         {:bioimageio_spec_version "0.4.8post1",
          :error                   "rdf does not have keys for :config :deepimagej",
          :status                  "failed",
          :name                    "Initial compatibility checks with DeepImageJ"}))
  (is (= (gen-summa-dict :comparison)
         {:bioimageio_spec_version "0.4.8post1",
          :error                   (:comparison errors/all-errors)
          :status                  "failed",
          :name                    (:reproduce errors/ci-stages)}))
  (is (= (gen-summa-dict :mistake-key)
         {:bioimageio_spec_version "0.4.8post1",
          :error                   "Other error",
          :status                  "failed",
          :name                    "Initial compatibility checks with DeepImageJ"}))
  (is (= (gen-summa-dict)
         {:bioimageio_spec_version "0.4.8post1",
          :status                  "passed",
          :name                    "Reproduce test outputs with DeepImageJ headless"})))

(defn no-pp [m]
  "finds model records without preprocessing (but with dij config)"
  (let [pp (some #(not (nil? (:script %))) (:p*process m))]
    (and (not pp) (:dij-config? m))))

(deftest no-pp-test
  (is (= 0 (count (filter no-pp @all-model-records)))))

(deftest write-test-summary-test
  (let [model (first @model-records)
        summa-dict (gen-summa-dict :no-dij-config)
        summa-path (get-in model [:paths :summa-path])
        expected-file (fs/file (fs/path summa-path "test_summary.yaml"))]
    (testing "Before the test, create empty directory for the test summary of model"
      (is (= (fs/absolutize summa-path)
             (fs/absolutize (create-summa-dir (get-in model [:paths :rdf-path]))))
          "Need absolute paths, in linux the absolute path is returned after creation"))
    (testing "List the directory to see the summary test was created"
      (write-test-summary! summa-dict model)
      (is (= (fs/path expected-file) (first (fs/list-dir summa-path)))))
    (testing "See that the contents of the test summery are correct (parse yaml)"
      (is (= (yaml/parse-string (slurp expected-file)) summa-dict)))
    (testing "After the tests, delete the file"
      (is (fs/delete-if-exists expected-file)))))

(deftest write-summaries-from-error!-test
  (let [{:keys [keep-testing error-found]}
        (discriminate/separate-by-error @model-records init-checks/errors-fns)
        one-error-k (first (select-keys error-found [:no-compatible-weights]))]
    (write-summaries-from-error! false one-error-k)))

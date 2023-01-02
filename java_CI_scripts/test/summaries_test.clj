(ns summaries-test
  (:require [collection :refer [COLLECTION-ROOT]]
            [summaries :refer :all]
            [downloads-test :refer [model-records all-model-records load-model-records]]
            [test-setup :refer [rdf-paths load-test-paths]]
            [clojure.test :refer [deftest is testing use-fixtures run-tests]]
            [clj-yaml.core :as yaml]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths load-model-records)

(deftest get-parent-components-test
  (testing "Using 2 arguments on a generic path"
    (let [a-path (fs/path "a_root" "first_folder" "second_folder" "file.txt")
          parent-comp (get-parent-components "a_root" a-path)
          expected-comp ["first_folder" "second_folder"]]
      (is (= (map str parent-comp) expected-comp))))
  (testing "Using only 1 argument (default COLLECTION-ROOT)"
    (let [parent-comp (get-parent-components @(:an-rdf rdf-paths))
          expected-parent-comp ["10.5281" "zenodo.6334881" "6346477"]]
      (is (= (map str parent-comp) expected-parent-comp)))))

(deftest new-root-path-test
  (let [a-path (fs/path "a_root" "first_folder" "second_folder" "file.txt")
        expect (fs/path "a_new_root" "first_folder" "second_folder")]
    (is (= (str expect) (str (new-root-path "a_root" "a_new_root" a-path))))))

(deftest gen-summa-path-test
  (let [expected-path (fs/path SUMMA-ROOT "10.5281" "zenodo.6334881" "6346477")]
    (is (= (str (gen-summa-path @(:an-rdf rdf-paths))) (str expected-path)))))

(deftest create-summa-dir-test
  (let [toy-root (fs/path "../toy_test_summaries")]
    (testing "Before folder exists"
      (is (not (fs/exists? toy-root))))
    (testing "Create folder structure"
      (let [summa-path (gen-summa-path COLLECTION-ROOT toy-root @(:an-rdf rdf-paths))
            result (create-summa-dir COLLECTION-ROOT toy-root @(:an-rdf rdf-paths))]
        (is (= (str (fs/absolutize summa-path)) (str (fs/absolutize result)))
            "successful creation returns the path")
        (is (= (map fs/file-name (fs/list-dir toy-root)) ["10.5281"]))))
    (testing "After tests, delete toy folder"
      (let [del (fs/delete-tree toy-root)]
        (is (= (str toy-root) (str del)) "successful delete returns the path")))))

(deftest gen-summa-dict-test
  (is (= (gen-summa-dict "failed" :initial :no-dij-config)
         {:bioimageio_spec_version "unknown",
          :bioimageio_core_version "unknown",
          :error "rdf does not have keys for config:deepimagej",
          :status "failed",
          :name "initial compatibility checks with deepimagej"})))

(defn no-pp [m]
  "finds model records without preprocessing (but with dij config)"
  (let [pp (some #(not (nil? (:script %))) (:p*process m))]
    (and (not pp) (:dij-config? m))))

(deftest no-pp-test
  (is (= 0 (count (filter no-pp @all-model-records)))))

(deftest write-test-summary-test
  (let [model (first @model-records)
        summa-dict (gen-summa-dict "failed" :initial :no-dij-config)
        summa-path (get-in model [:paths :summa-path])
        expected-file (fs/file (fs/path summa-path "test_summary.yaml"))]
    (testing "Before the test, create empty directory for the test summary of model"
      (is (= (fs/absolutize summa-path)
             (fs/absolutize (create-summa-dir (get-in model [:paths :rdf-path]))))
          "Need absolute paths, in linux the absolute path is returned after creation"))
    (testing "List the directory to see the summary test was created"
      (write-test-summary model summa-dict)
      (is (= (fs/path expected-file) (first (fs/list-dir summa-path)))))
    (testing "See that the contents of the test summery are correct (parse yaml)"
      (is (= (yaml/parse-string (slurp expected-file)) summa-dict)))
    (testing "After the tests, delete the file"
      (is (fs/delete-if-exists expected-file)))))
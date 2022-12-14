(ns summaries-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-tests]]
            [summaries :refer :all]
            [collection :refer [COLLECTION-ROOT]]
            [babashka.fs :as fs]))

(def test-rdf-path (atom nil))

(defn load-test-data
  [test-fn]
  (reset! test-rdf-path
          (fs/path (str COLLECTION-ROOT) "10.5281" "zenodo.6334881" "6346477" "rdf.yaml"))
  (test-fn))

(use-fixtures :once load-test-data)

(deftest get-parent-components-test
  (let [parent-dir (get-parent-components @test-rdf-path)
        expected-parent-dir ["10.5281" "zenodo.6334881" "6346477"]]
    (is (= (map str parent-dir) expected-parent-dir))))

(deftest get-summa-path-test
  (let [expected-path (fs/path SUMMA-ROOT "10.5281" "zenodo.6334881" "6346477")]
    (is (= (str (get-summa-path @test-rdf-path)) (str expected-path)))))

(deftest create-summa-dir-test
  (let [toy-root (fs/path "../toy_test_summaries")]
    (testing "Before folder exists"
      (is (not (fs/exists? toy-root))))
    (testing "Create folder structure"
      (let [summa-path (get-summa-path toy-root @test-rdf-path)
            res (create-summa-dir toy-root @test-rdf-path)]
        (is (= (str (fs/absolutize summa-path)) (str (fs/absolutize res)))
            "successful creation returns the path")
        (is (= (map fs/file-name (fs/list-dir toy-root)) ["10.5281"]))))
    (testing "After tests, delete toy folder"
      (let [del (fs/delete-tree toy-root)]
        (is (= (str toy-root) (str del)) "successful delete returns the path")))))
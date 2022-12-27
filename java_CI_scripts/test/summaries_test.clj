(ns summaries-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-tests]]
            [summaries :refer :all]
            [collection :refer [COLLECTION-ROOT]]
            [test-setup :refer [rdf-paths load-test-paths]]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths)

(deftest get-parent-components-test
  (let [parent-dir (get-parent-components @(:an-rdf rdf-paths))
        expected-parent-dir ["10.5281" "zenodo.6334881" "6346477"]]
    (is (= (map str parent-dir) expected-parent-dir))))

(deftest get-summa-path-test
  (let [expected-path (fs/path SUMMA-ROOT "10.5281" "zenodo.6334881" "6346477")]
    (is (= (str (get-summa-path @(:an-rdf rdf-paths))) (str expected-path)))))

(deftest create-summa-dir-test
  (let [toy-root (fs/path "../toy_test_summaries")]
    (testing "Before folder exists"
      (is (not (fs/exists? toy-root))))
    (testing "Create folder structure"
      (let [summa-path (get-summa-path toy-root @(:an-rdf rdf-paths))
            res (create-summa-dir toy-root @(:an-rdf rdf-paths))]
        (is (= (str (fs/absolutize summa-path)) (str (fs/absolutize res)))
            "successful creation returns the path")
        (is (= (map fs/file-name (fs/list-dir toy-root)) ["10.5281"]))))
    (testing "After tests, delete toy folder"
      (let [del (fs/delete-tree toy-root)]
        (is (= (str toy-root) (str del)) "successful delete returns the path")))))
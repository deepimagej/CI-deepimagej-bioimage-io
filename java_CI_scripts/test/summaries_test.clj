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

; create folder, check if correct, then delete it!
(deftest create-summa-dir-test
  (let [summa-path (get-summa-path @test-rdf-path)
        res (create-summa-dir @test-rdf-path)]
    (is (= (str (fs/absolutize summa-path)) (str (fs/absolutize res))))
    ; list dir
    ; delete (see if it doesnt exists)
    ))
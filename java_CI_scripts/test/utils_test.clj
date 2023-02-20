(ns utils-test
  (:require [utils :refer :all]
            [test-setup :refer :all]
            [clojure.test :refer :all]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths load-rdfs-parsed load-model-records)

(deftest count-dict-test
  (is (= (count-dict {:a [1 2] :b [4 5 6]}) {:a 2 :b 3})))

(deftest select-key->vec-test
  (is (= (select-key->vec {:a 1 :b "2"} :b) [:b "2"])))

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
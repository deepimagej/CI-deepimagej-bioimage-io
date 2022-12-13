(ns collection-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-tests run-test]]
            [collection :refer :all]
            [babashka.fs :as fs]))

(deftest collection-root-test
  (is (= (fs/file-name COLLECTION-ROOT) "rdfs"))
  (is (= (mapv fs/file-name (fs/list-dir COLLECTION-ROOT))
         ["10.5281" "bioimageio" "deepimagej" "fiji" "hpa" "ilastik" "imjoy" "zero"])
      "This test may fail if new partners/zenodo dois are added to the zoo"))

(deftest str-json->vector-test
  (testing "Input is a raw json string"
    (let [str-json "{\"include\": [{\"resource_id\": \"**\", \"version_id\": \"**\"}]}"
          parsed (str-json->vector str-json)]
      (is (= (count parsed)) 1)
      (is (= (:resource_id (nth (str-json->vector str-json) 0)) "**")))))

(deftest file-json->vector-test
 (testing "Input is a json file (java.io.File)"
   (let [parsed (file-json->vector (fs/file "pending_matrix/two_models.json"))
         [m1 _] parsed
         {:keys [resource_id version_id]} m1]
     (is (= (count parsed) 2))
     (is (= resource_id "10.5281/zenodo.7261974"))
     (is (= version_id "7261975"))))
 (testing "Input is a string that is a path to a json file"
   (let [parsed (file-json->vector "./pending_matrix/two_models.json")
         [_ m2] parsed
         {:keys [resource_id version_id]} m2]
     (is (= (count parsed) 2))
     (is (= resource_id "deepimagej/DeepSTORMZeroCostDL4Mic"))
     (is (= version_id "latest")))))

(deftest -main-test
  (is (= '("a" "1" 2) (-main "a" "1" 2))))
(run-tests 'collection-test)

(deftest filter-rdfs-test
  (let [all-rdfs (fs/glob COLLECTION-ROOT "**")
        some-rdfs (fs/glob (fs/path COLLECTION-ROOT "10.5281/zenodo.5749843") "**")]
    (is (= (count (filter-rdfs all-rdfs)) 164))
    (is (= (count (filter-rdfs some-rdfs)) 2))))

; TODO finish this
(deftest resource->paths-test
  (let [all-models (first (file-json->vector "pending_matrix/all_models.json"))
        version-glob {:resource_id "10.5281/zenodo.5749843" :version_id "**"}
        one-model (last (file-json->vector "pending_matrix/two_models.json"))
        resource->paths-root (partial resource->paths COLLECTION-ROOT)]
    (testing "globbing all resources and versions"
      (is (= (count (resource->paths-root all-models)) 164)))
    (testing "globbing all version of a resource"
      (is (= (count (resource->paths-root version-glob)) 2)))
    (testing "paths of a single model (resource and version"
      (is (= (count (resource->paths-root one-model)) 1)))))

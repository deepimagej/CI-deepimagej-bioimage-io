(ns collection-test
  (:require [config :refer [ROOTS]]
            [collection :refer :all]
            [clojure.test :refer :all]
            [clojure.set :as set]
            [babashka.fs :as fs]))

(deftest collection-root-test
  (is (= (fs/file-name (:collection-root ROOTS)) "rdfs"))
  (is (set/subset? #{"10.5281" "bioimageio" "deepimagej" "fiji" "hpa" "ilastik" "imjoy" "zero"}
                   (set (mapv fs/file-name (fs/list-dir (:collection-root ROOTS)))))))

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

(deftest filter-rdfs-test
  (let [all-rdfs (fs/glob (:collection-root ROOTS) "**")
        some-rdfs (fs/glob (fs/path (:collection-root ROOTS) "10.5281/zenodo.5749843") "**")]
    (testing "Number of rdfs in the collection"
      (is (>= (count (filter-rdfs all-rdfs)) 165)))
    (is (= (count (filter-rdfs some-rdfs)) 2))))

(deftest resource->paths-test
  (let [all-models-resources (first (file-json->vector "pending_matrix/all_models.json"))
        version-glob (first (file-json->vector "pending_matrix/two_versions.json"))
        one-model (last (file-json->vector "pending_matrix/two_models.json"))]
    (testing "Globbing all resources and versions"
      (is (>= (count (resource->paths all-models-resources)) 165)))
    (testing "Globbing all version of a resource"
      (is (= (count (resource->paths version-glob)) 2))
      (is (= (resource->paths version-glob)
             (resource->paths {:resource_id "10.5281/zenodo.5749843" :version_id "**"}))))
    (testing "Paths of a single model (resource and version"
      (is (= (count (resource->paths one-model)) 1)))))

(deftest get-rdfs-to-test-test
  (let [filenames ["all_models.json" "two_models.json" "two_versions.json"]
        files (map #(fs/file "pending_matrix" %) filenames)
        resource-vectors (map file-json->vector files)
        [r-a r-b r-c] (map get-rdfs-to-test resource-vectors)]
    (testing "Resource vector of 1 element but complete globbing"
      (is (>= (count r-a) 165)))
    (testing "Resource vector has 2 maps"
      (is (= (set (map #(fs/file-name (fs/parent %)) r-b)) #{"7261975" "latest"})))
    (testing "Resource vector has 1 map, but version globbing"
      (is (= (set (map #(fs/file-name (fs/parent %)) r-c)) #{"5888237" "5877226"})))))

(deftest parse-collection-test
  (testing "Contents of collection.json"
    (let [parsed (parse-collection)
          {:strs [model dataset application notebook] :as freqs}
          (frequencies (map :type parsed))]
      (is (= (set (keys freqs)) #{"model" "dataset" "application" "notebook"}))
      (is (>= model 46))
      (is (>= dataset 42))
      (is (>= application 49))
      (is (>= notebook 2)))))

(deftest get-model-identifier-test
  (let [models (filter #(= (:type %) "model") (parse-collection))
        a-model (first (filter #(= (:nickname %) "impartial-shrimp") models))]
    (is (= (get-model-identifier a-model)
           {:resource_id "10.5281/zenodo.5874741", :version_id "5874742"}))))

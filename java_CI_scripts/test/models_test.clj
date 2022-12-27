(ns models-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-tests]]
            [models :refer :all]
            [collection :refer [COLLECTION-ROOT]]
            [test-setup :refer [test-rdf-path load-test-data]]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-data)

(deftest parse-model-test
  (let [model-dict (parse-model @test-rdf-path)]
    (is (= "laid-back-lobster" (get-in model-dict [:config :bioimageio :nickname])))
    (is (= "13bbeb9a2403f5ff840951d8907586cf1ceded3072d36466db3e592b5ad53649"
           (get-in model-dict [:weights :pytorch_state_dict :sha256])))))

(def model-1 "A tensorflow model (with keras)"
  (parse-model (fs/path COLLECTION-ROOT "10.5281" "zenodo.5749843" "5888237" "rdf.yaml")))

(def model-2 "A torchscript model (with state dict)"
  (parse-model (fs/path COLLECTION-ROOT "10.5281" "zenodo.5874741" "5874742" "rdf.yaml" )))

(deftest get-weight-info-test
  (let [w (get-weight-info (parse-model @test-rdf-path))
        w1 (get-weight-info model-1)
        w2 (get-weight-info model-2)
        w3 (get-weight-info :torchscript model-1)]
    (testing "rdf with 1 incompatible weight (pytorch state dict)"
      (is (= 1 (count w)))
      (is (= (->Weight nil "https://zenodo.org/api/files/4a69ddca-3874-4469-b11d-b9ed626d197f/confocal_pnas_2d.pytorch")
             (first w))))
    (testing "rdf with Tensorflow weights"
      (is (= 2 (count w1)))
      (is (nil? (:type (first w1))))
      (is (= "Tensorflow" (:type (second w1)))))
    (testing "rdf with Pytorch weights"
      (is (= 2 (count w2)))
      (is (nil? (:type (first w2))))
      (is (= "Pytorch" (:type (second w2)))))
    (testing "rdf with incorrect keyword"
      (is (= w3 (->Weight "Pytorch" nil))))))

(deftest get-p*process-info-test
  (let [p (get-p*process-info (parse-model @test-rdf-path))
        p1 (get-p*process-info model-1)
        pp-dict-2 (get-in model-2 [:config :deepimagej :prediction])]
    (testing "rdf with no preprocessing"
      (is (= [] (get-p*process-info p))))
    (testing "rdf with pre- and post-processing"
      (is (= (set p1) (set [(->PProcess :post-p "binarize.ijm")
                            (->PProcess :pre-p "per_sample_scale_range.ijm")]))))
    (testing "rdf with only pre-processing"
      (is (= (get-p*process-info :postprocess pp-dict-2)
             (->PProcess :post-p nil)))
      (is (= (get-p*process-info :preprocess pp-dict-2)
             (->PProcess :pre-p "zero_mean_unit_variance.ijm"))))))

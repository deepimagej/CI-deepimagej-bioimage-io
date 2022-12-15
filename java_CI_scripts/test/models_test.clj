(ns models-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-tests]]
            [models :refer :all]
            [test-setup :refer [test-rdf-path load-test-data]]))

(use-fixtures :once load-test-data)

(deftest parse-model-test
  (let [model-dict (parse-model @test-rdf-path)]
    (is (= "laid-back-lobster" (get-in model-dict [:config :bioimageio :nickname])))
    (is (= "13bbeb9a2403f5ff840951d8907586cf1ceded3072d36466db3e592b5ad53649"
           (get-in model-dict [:weights :pytorch_state_dict :sha256])))))

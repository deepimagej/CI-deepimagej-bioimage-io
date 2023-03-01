(ns core.sandbox
  (:require [config :refer :all]
            utils
            collection
            models
            [summaries summary errors init-checks]
            [downloads download]
            [reproduce communicate run-fiji-scripts]
            [core main]
            [test-setup :refer :all]
            [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths load-model-records load-rdfs-parsed)

(deftest dummy-test
  (testing "dummy test to load everything form test-setup"))

; run all tests to load the data from test-setup
(run-tests)

(comment
  "models that have the :run_model key"
  (filter (fn [[k v]] (not (nil? v)))
          (map #(vector (:name %) (get-in % [:run_mode])) @all-rdfs-parsed))
  )

; paths parsed and records of models in use-cases


; try-catch inside let
(comment
  (let [a (try (/ 4 0) (catch Exception e nil))]
    a)
  )

; rdfs that are models
(comment
  (frequencies (map :type @all-rdfs-parsed))
  ; => {"model" 69, "dataset" 42, "application" 52, "notebook" 2}
  (filter (fn [r] (= "model" (:type r))) @all-rdfs-parsed)
  )

; rdf locals
(comment
  (def rdfs (collection/get-rdfs-to-test
              (collection/file-json->vector (fs/file "pending_matrix/local.json"))))
  (def parsed (map models/parse-model rdfs))
  (def models (filter summaries.init-checks/model? (map models/build-model rdfs)))
  (def dois (map #(get-in % [:config :bioimageio :doi]) parsed))
  )
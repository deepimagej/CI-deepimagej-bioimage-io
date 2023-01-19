(ns downloads.initial-checks-test
  (:require [downloads.initial-checks :refer :all]
            [test-setup :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :once load-test-paths load-rdfs-parsed load-model-records load-model-rp's)

(defn count-dict
  "counts dictionary entries that are seqs"
  [d] (reduce (fn [m [k v]] (assoc m k (count v))) {}  d))

(deftest count-dict-test
  (is (= (count-dict {:a [1 2] :b [4 5 6]}) {:a 2 :b 3})))

(deftest dij-config?-test
  (is (= 2 (count (filter dij-config? @model-rp's))))
  (is (= 48 (count (filter dij-config? @all-model-rp's)))))

(deftest no-run-mode?-test
  (let [{without-rm true with-rm false} (group-by no-run-mode? @all-model-rp's)]
    (is (= 161 (count without-rm)))
    (is (= 4 (count with-rm)))
    (is (= (repeat 4 "deepimagej")
           (mapv #(get-in % [:parsed-rdf :run_mode :name]) with-rm)))))

(deftest any-compatible-weight?-test
  (is (not (any-compatible-weight? (first @model-rp's))))
  (is (any-compatible-weight? (second @model-rp's)))
  (is (any-compatible-weight? (last @model-rp's)))
  (testing "all models in collection with compatible weights"
    (is (= 46 (count (filter any-compatible-weight? @all-model-rp's))))))

(deftest available-sample-images?-test
  (is (= 2 (count (filter available-sample-images? @model-rp's))))
  (is (= 29 (count (filter available-sample-images? @all-model-rp's)))))


(deftest p*process-in-attachment?-test
  (is (not (p*process-in-attachment?
             (assoc-in (last @model-rp's) [:model-record :attach] []))))
  (is (p*process-in-attachment? (second @model-rp's))))

(deftest error-functions-test
  (let [initial-error? (partial contains? (set (keys summaries.errors/initial-errors)))]
    (testing "errors correspond to the same ones in summaries.errors"
      (is (->> (keys error-functions) (map initial-error?) and)))))

(deftest check-error-test
  (let [discd-models (check-error {:keep-testing @model-rp's}
                                  (first (select-keys error-functions [:no-dij-config])))
        d-all-models-dijconfig (check-error {:keep-testing @all-model-rp's}
                                            (first (select-keys error-functions [:no-dij-config])))
        d-all-models-runmode (check-error d-all-models-dijconfig
                                          (first (select-keys error-functions [:key-run-mode])))]
    (testing "No-dij-config for the 3 variety models"
      (is (= {:error-found 1, :keep-testing 2} (count-dict discd-models)))
      (is (= {:no-dij-config 1} (count-dict (:error-found discd-models)))))
    (testing "No-dij-config for all models-rp"
      (is (= {:error-found 1, :keep-testing 48} (count-dict d-all-models-dijconfig)))
      (is (= {:no-dij-config 117} (count-dict (:error-found d-all-models-dijconfig)))))
    (testing "All models, with run-mode error after discriminating with deepimagej config"
      (is (= {:error-found 2, :keep-testing 43}) (count-dict d-all-models-runmode))
      (is (= {:key-run-mode 4 :no-dij-config 117} (count-dict (:error-found d-all-models-runmode)))))))

(deftest separate-by-error-test
  (let [models-discriminated (separate-by-error @model-rp's)
        all-models-discriminated (separate-by-error @all-model-rp's
                                                    (select-keys error-functions
                                                                 [:no-dij-config :key-run-mode]))]
    (testing "Variety models"
      (is (= (count-dict models-discriminated)
             {:error-found 5, :keep-testing 2}))
      (is (= (count-dict (:error-found models-discriminated))
             {:no-dij-config 0, :no-sample-images 0, :no-compatible-weights 1,
              :key-run-mode 0, :no-p*process 0})))
    (testing "All models, with only no-dij-config and key-run-mode errors"
      (is (= (count-dict all-models-discriminated)
             {:error-found 2, :keep-testing 44}))
      (is (= (count-dict (:error-found all-models-discriminated))
             {:key-run-mode 4 :no-dij-config 117})))))

; this has been replaced by more general version, but test can stay
(deftest separate-by-dij-config-test
  (testing "The 3 models tf, pt and state-dict (incompatible with dij)"
    (let [separated (separate-by-dij-config @model-records)]
      (is (= (keys separated) [:keep-testing :no-dij-config]))
      (is (= 2 (count (:keep-testing separated))))
      (is (= 1 (count (:no-dij-config separated))))))
  (testing "All models in the collection"
    (let [separated (separate-by-dij-config @all-model-records)]
      (is (= {:keep-testing 48 :no-dij-config 117} (count-dict separated))))))






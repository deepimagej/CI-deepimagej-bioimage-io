(ns summaries.discriminate-test
  (:require [summaries.discriminate :refer :all]
            utils
            [summaries.init-checks :as init-checks]
            [summaries.reproduce-checks :as reproduce-checks]
            [test-setup :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :once load-test-paths load-rdfs-parsed load-model-records)

(deftest check-error-test
  (testing "Errors during the init stage of CI"
    (let [d-models-dijconfig (check-error {:keep-testing @model-records}
                                          (utils/select-key->vec init-checks/errors-fns :no-dij-config))
          d-all-models-dijconfig (check-error {:keep-testing @all-model-records}
                                              (utils/select-key->vec init-checks/errors-fns :no-dij-config))
          d-all-models-runmode (check-error d-all-models-dijconfig
                                            (utils/select-key->vec init-checks/errors-fns :key-run-mode))]
      (testing "No-dij-config for the 3 variety models"
        (is (= (utils/count-dict d-models-dijconfig) {:error-found 1, :keep-testing 2} ))
        (is (= (utils/count-dict (:error-found d-models-dijconfig)) {:no-dij-config 1})))
      (testing "No-dij-config for all models"
        (is (= (utils/count-dict d-all-models-dijconfig) {:error-found 1, :keep-testing 48}))
        (is (>= (:no-dij-config (utils/count-dict (:error-found d-all-models-dijconfig))) 117)))
      (testing "All models, with run-mode error after discriminating with deepimagej config"
        (is (= (utils/count-dict d-all-models-runmode) {:error-found 2, :keep-testing 44}))
        (is (>= (:no-dij-config (utils/count-dict (:error-found d-all-models-runmode))) 117)))))
  (testing "Errors during reproduce stage of CI"
    (let [d-models-dij-headless (check-error {:keep-testing @model-records}
                                             (utils/select-key->vec reproduce-checks/errors-fns :dij-headless))
          d-models-comparison (check-error d-models-dij-headless
                                           (utils/select-key->vec reproduce-checks/errors-fns :comparison))]
      (is (= (utils/count-dict d-models-dij-headless) {:error-found 1, :keep-testing 2}))
      (is (= (utils/count-dict (:error-found d-models-dij-headless)) {:dij-headless 1}))
      (is (= (utils/count-dict d-models-comparison) {:error-found 2, :keep-testing 1}))
      (is (= (utils/count-dict (:error-found d-models-comparison))
             {:dij-headless 1 :comparison 1}))
      )))

(deftest separate-by-error-test
  (let [models-discriminated (separate-by-error @model-records init-checks/errors-fns)
        all-models-discriminated (separate-by-error @all-model-records
                                                    (select-keys init-checks/errors-fns
                                                                 [:no-dij-config :key-run-mode]))]
    (testing "Variety models"
      (is (= (utils/count-dict models-discriminated)
             {:error-found 5, :keep-testing 2}))
      (is (= (utils/count-dict (:error-found models-discriminated))
             {:no-dij-config 0, :no-sample-images 0, :no-compatible-weights 1,
              :key-run-mode 0, :no-p*process 0})))
    (testing "All models, with only no-dij-config and key-run-mode errors"
      (is (= (utils/count-dict all-models-discriminated)
             {:error-found 2, :keep-testing 44}))
      (is (>= (:no-dij-config (utils/count-dict (:error-found all-models-discriminated))) 117)))))

; this has been replaced by more general version, but test can stay
(deftest separate-by-dij-config-test
  (testing "The 3 models tf, pt and state-dict (incompatible with dij)"
    (let [separated (separate-by-dij-config @model-records)]
      (is (= (keys separated) [:keep-testing :no-dij-config]))
      (is (= 2 (count (:keep-testing separated))))
      (is (= 1 (count (:no-dij-config separated))))))
  (testing "All models in the collection"
    (let [separated (separate-by-dij-config @all-model-records)]
      (is (>= (:keep-testing (utils/count-dict separated) 48)))
      (is (>= (:no-dij-config (utils/count-dict separated) 117))))))

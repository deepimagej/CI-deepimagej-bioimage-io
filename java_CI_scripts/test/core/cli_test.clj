(ns core.cli-test
  (:require [clojure.test :refer :all]
            [core.cli :refer :all]))

(def a-valid-json "{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"},{\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}")
; '{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"}, {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}'
; '{\"include\": [{\"resource_id\": \"**\", \"version_id\": \"**\"}]}'

(deftest valid-json?-test
  (is (not (valid-json? "a")))
  (is (valid-json? a-valid-json)))

(deftest validate-args-test
  (let [default-dict {:options {:unit-test false,
                                :json-file "./pending_matrix/two_models.json"},
                      :action nil}]
    (is (= (validate-args []) default-dict))
    (is (= (validate-args ["-u"]) (assoc-in default-dict [:options :unit-test] true)))
    (is (= (validate-args ["an-action"]) (assoc default-dict :action "an-action")))
    (is (= (validate-args ["-x"])
           {:exit-message (str error-title "Unknown option: \"-x\"")}))
    (testing "-h option"
      (let [h-return (validate-args ["-h"])]
        (is (= (keys h-return) [:exit-message :ok?]))
        (is (:ok? h-return))
        (is (string? (:exit-message h-return)))))
    (testing "-s option"
      (let [str-json " {\"a\": 1}" ]
        (is (= (validate-args [(str "-s" str-json)])
               (assoc-in default-dict [:options :json-string] str-json)))
        (is (= (validate-args ["-s" "a"])
               {:exit-message (str error-title "Failed to validate \"-s a\": String must be valid json")}))))
    (testing "-j option"
      (is (= (validate-args ["-j"])
             {:exit-message (str error-title "Missing required argument for \"-j FILE\"")}))
      (is (= (validate-args ["-j" "./a.txt"])
             {:exit-message (str error-title "Failed to validate \"-j ./a.txt\": File must exist")}))
      (is (= (validate-args ["-j" "Readme.md"])
             {:exit-message (str error-title "Failed to validate \"-j Readme.md\": File must contain valid json")}))
      (is (= (validate-args ["-j" "pending_matrix/two_models.json"])
             (assoc-in default-dict [:options :json-file] "pending_matrix/two_models.json"))))))

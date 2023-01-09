(ns core.main
  (:require [summaries :refer [create-summa-dir gen-summa-dict write-test-summary]]
            [collection :refer [get-rdfs-to-test file-json->vector str-json->vector]]
            [models :refer [create-model-dir build-model]]
            [downloads.initial-checks :refer [separate-by-dij-config]]
            [reproduce.communicate :refer [build-dij-model write-comm-file]]
            [downloads initial-checks-test download-test p-process-test]
            collection-test models-test summaries-test reproduce.communicate-test core.cli-test
            [core.cli :refer [validate-args exit]]
            [clojure [test :refer [run-tests]]]))

;TODO heavy refactor on action and error-name
(defn initial-pipeline
  "Creates the folders corresponding to test input json"
  [json-type options]
  (let [parsing-function (json-type {:json-file   file-json->vector
                                     :json-string str-json->vector})
        rdfs (get-rdfs-to-test (parsing-function (json-type options)))
        model-records (map build-model rdfs)
        {:keys [no-dij-config keep-testing]} (separate-by-dij-config model-records)
        failed-dict (gen-summa-dict "failed" :initial :no-dij-config)]
    (println "Creating dirs for test summaries")
    (mapv create-summa-dir rdfs)
    (println "Creating dirs for models")
    (mapv create-model-dir rdfs)
    (println "Creating test summaries for" (count no-dij-config) "models")
    (mapv #(write-test-summary % failed-dict) no-dij-config)
    (println "Creating comm file for" (count keep-testing) "models")
    (write-comm-file (map build-dij-model keep-testing))))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (cond
        (:unit-test options)
        (run-tests 'collection-test 'summaries-test 'models-test
                   'downloads.initial-checks-test 'downloads.download-test
                   'downloads.p-process-test 'reproduce.communicate-test 'core.cli-test)
        (:json-string options)
        (initial-pipeline :json-string options)
        :else
        (initial-pipeline :json-file options)))))
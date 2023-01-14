(ns core.main
  (:require [collection :refer [get-rdfs-to-test file-json->vector str-json->vector]]
            [models :refer [create-model-dir build-model parse-model]]
            [summaries.summary :as summary]
            [downloads.initial-checks :as initial-checks :refer [separate-by-dij-config]]
            [reproduce.communicate :refer [build-dij-model write-comm-file]]
            [downloads initial-checks-test download-test p-process-test]
            collection-test models-test summary-test reproduce.communicate-test core.cli-test
            [core.cli :refer [validate-args exit]]
            [clojure [test :refer [run-tests]]]))

;TODO refactor on actions (?)
(defn initial-pipeline
  "Creates the folders corresponding to test input json"
  [json-type options]
  (let [parsing-function (json-type {:json-file   file-json->vector
                                     :json-string str-json->vector})
        rdfs-paths (get-rdfs-to-test (parsing-function (json-type options)))
        model-records (map build-model rdfs-paths)
        rdfs-parsed (map parse-model rdfs-paths)
        models-rp (map #(initial-checks/map->ModelRP {:model-record %1 :parsed-rdf %2}) model-records rdfs-parsed)
        {:keys [keep-testing error-found]} (initial-checks/separate-by-error models-rp)
        ;{:keys [no-dij-config keep-testing]} (separate-by-dij-config model-records)
        failed-dict (summary/gen-summa-dict "failed" :initial :no-dij-config)]
    (println "Creating dirs for test summaries")
    (mapv summary/create-summa-dir rdfs-paths)
    (println "Creating dirs for models")
    (mapv create-model-dir rdfs-paths)

    ;(println "Creating test summaries for" (count no-dij-config) "models")
    ;(mapv #(summary/write-test-summary! % failed-dict) no-dij-config)

    (mapv summary/write-summaries-from-error! error-found)
    (println "Creating comm file for" (count keep-testing) "models")
    (write-comm-file (map build-dij-model (:model-record keep-testing)))))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (cond
        (:unit-test options)
        (run-tests 'collection-test 'summary-test 'models-test
                   'downloads.initial-checks-test 'downloads.download-test
                   'downloads.p-process-test 'reproduce.communicate-test 'core.cli-test)
        (:json-string options)
        (initial-pipeline :json-string options)
        :else
        (initial-pipeline :json-file options)))))
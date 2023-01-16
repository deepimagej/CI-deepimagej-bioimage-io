(ns core.main
  (:require collection
            models
            [summaries.summary :as summary]
            [downloads.initial-checks :as initial-checks]
            [reproduce.communicate :as comm]
            [core cli unit-tests]))

;TODO refactor on actions (if implemented in core.cli?)
(defn initial-pipeline
  "Creates the folders corresponding to test input json"
  [json-type options]
  (let [parsing-function (json-type {:json-file   collection/file-json->vector
                                     :json-string collection/str-json->vector})
        rdfs-paths (collection/get-rdfs-to-test (parsing-function (json-type options)))
        model-records (map models/build-model rdfs-paths)
        rdfs-parsed (map models/parse-model rdfs-paths)
        models-rp (map #(initial-checks/map->ModelRP {:model-record %1 :parsed-rdf %2})
                       model-records rdfs-parsed)
        {:keys [keep-testing error-found]} (initial-checks/separate-by-error models-rp)]
    (println "Creating dirs for test summaries")
    (mapv summary/create-summa-dir rdfs-paths)
    (println "Creating dirs for models")
    (mapv models/create-model-dir rdfs-paths)

    (mapv summary/write-summaries-from-error! error-found)
    (println "Creating comm file for" (count keep-testing) "models")
    (comm/write-comm-file (map #(comm/build-dij-model (:model-record %)) keep-testing))))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (core.cli/validate-args args)]
    (if exit-message
      (core.cli/exit (if ok? 0 1) exit-message)
      (cond
        (:unit-test options)
        (core.unit-tests/run-all-tests)
        (:json-string options)
        (initial-pipeline :json-string options)
        :else
        (initial-pipeline :json-file options)))))
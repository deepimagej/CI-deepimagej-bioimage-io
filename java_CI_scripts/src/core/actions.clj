(ns core.actions
  "Define the steps for every action: init, download, and reproduce"
  (:require [config :refer [FILES CONSTANTS]]
            utils
            collection
            models
            [summaries [summary :as summary] [reports :as reports] [discriminate :as discriminate]
             [init-checks :as init-checks] [reproduce-checks :as reproduce-checks]]
            [downloads [download :as download]]
            [reproduce [communicate :as comm] [run-fiji-scripts :as run-fiji-scripts]]
            [babashka.fs :as fs]
            [babashka.process :as pr]))

(defn initial-pipeline
  "Creates the corresponding folders to test the rdfs specified in the input json"
  [ini-return json-type options]
  (let [parsing-function (json-type {:json-file   collection/file-json->vector
                                     :json-string collection/str-json->vector})
        rdfs-paths (collection/get-rdfs-to-test (parsing-function (json-type options)))
        model-records (filter init-checks/model? (map models/build-model rdfs-paths))
        {:keys [keep-testing error-found]}
        (discriminate/separate-by-error model-records init-checks/errors-fns)]
    (println "Creating dirs for test summaries")
    (mapv summary/create-summa-dir rdfs-paths)
    ; Reset contents of test_summaries/Readme.md
    (spit (:summa-readme FILES)
          (str (utils/original-file-content (:summa-readme FILES) (:summa-readme-header CONSTANTS))
               (:summa-readme-header CONSTANTS) (System/lineSeparator)))
    (println "Creating dirs for models")
    (mapv models/create-model-dir (map #(get-in % [:paths :rdf-path]) keep-testing))

    (mapv summary/write-summaries-from-error! error-found)
    (utils/print-and-log (format "- Creating comm file for %d models\n" (count keep-testing))
                         (:summa-readme FILES))
    ; edn with all the models and args (not used anymore, here for legacy reasons)
    (comm/write-comm-file (map comm/build-dij-model keep-testing)
                          (fs/file (System/getProperty "user.home") "models_to_test.edn"))

    ;txt input to numpy-tiff repo, alternatively do not check the :no-sample-images error during initial checks
    (comm/write-absolute-paths (concat keep-testing (:no-sample-images error-found) (:key-run-mode error-found))
                               :rdf-path (fs/file ".." "numpy-tiff-deepimagej" "resources" "rdfs_to_test.txt"))
    (comm/write-absolute-paths (:no-dij-config error-found) :rdf-path (fs/file "rdfs_with_no_dij_config.txt"))
    (comm/write-absolute-paths keep-testing :model-dir-path (:models-listed FILES))
    (comm/write-absolute-paths keep-testing :rdf-path (:rdfs-listed FILES))
    (mapv comm/write-dij-model keep-testing)
    (if ini-return keep-testing)))

(defn download-pipeline
  "Downloads files necessary for testing the models"
  [json-type options]
  (let [model-records-keep (initial-pipeline true json-type options)]
    (printf "Populating model folders for %d models \n" (count model-records-keep))
    (doall (map download/populate-model-folder model-records-keep))
    (printf "Downloading files (this could take some minutes) \n")
    (flush)
    (let [timed (utils/my-time (doall (pmap download/download-into-model-folder model-records-keep)))]
      (printf "Total Time Taken: %s\n" (:iso timed)) (flush))))

(defn reproduce-pipeline
 "Reproduce pipeline for Windows and linux/mac, where reproduce.run-fiji-scripts fails"
  [& _]
  ; Run inference on Fiji
  (run-fiji-scripts/grant-exec-permission)
  (run-fiji-scripts/build-bash-script (:bash-script FILES))
  (if (fs/windows?)
    (run-fiji-scripts/-main)
    (let [timed (utils/my-time (pr/shell "sh" (:bash-script FILES)))]
      (printf "Total Time Taken: %s\n" (:iso timed))
      (flush)))
  ; Generate final test summaries
  (let [rdf-paths (map fs/path (utils/read-lines (:rdfs-listed FILES)))
        models-tested (map models/build-model rdf-paths)
        {:keys [keep-testing error-found]}
        (discriminate/separate-by-error models-tested reproduce-checks/errors-fns)]
    (mapv summary/write-summaries-from-error! error-found)
    (mapv (partial summary/write-test-summary! (summary/gen-summa-dict)) keep-testing)
    (utils/print-and-log (format "- Created %3d test summaries for models that pass the CI\n" (count keep-testing))
                         (:summa-readme FILES)))
  ; Generate the report
  (reports/basic-report))
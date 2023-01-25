(ns core.actions
  "Define the steps for every action:
  - init
  - download
  - reproduce"
  (:require [summaries.summary :as summary]
            [downloads [initial-checks :as initial-checks] [download :as download]]
            [reproduce.communicate :as comm]
            [babashka.fs :as fs]))

;TODO refactor on actions (if implemented in core.cli?)
(defn initial-pipeline
  "Creates the folders corresponding to test input json"
  [json-type options ini-return]
  (let [parsing-function (json-type {:json-file   collection/file-json->vector
                                     :json-string collection/str-json->vector})
        rdfs-paths (collection/get-rdfs-to-test (parsing-function (json-type options)))
        model-records (map models/build-model rdfs-paths)
        rdfs-parsed (map models/parse-model rdfs-paths)
        models-rp (map #(initial-checks/map->ModelRP {:model-record %1 :parsed-rdf %2})
                       model-records rdfs-parsed)
        {:keys [keep-testing error-found]} (initial-checks/separate-by-error models-rp)
        model-records-keep (map #(:model-record %) keep-testing)]
    (println "Creating dirs for test summaries")
    (mapv summary/create-summa-dir rdfs-paths)
    (println "Creating dirs for models")
    (mapv models/create-model-dir (map #(get-in % [:paths :rdf-path]) model-records-keep))

    (mapv summary/write-summaries-from-error! error-found)
    (println "Creating comm file for" (count keep-testing) "models")
    (comm/write-comm-file (map comm/build-dij-model model-records-keep))
    (comment
      ; input to numpy-tiff repo, needs that the :no-sample-images error is not checked during initial checks
      (comm/write-absolute-paths model-records-keep :rdf-path
                                 (fs/file ".." "numpy-tiff-deepimagej" "resources" "rdfs_to_test.txt")))
    (comm/write-absolute-paths model-records-keep :model-dir-path
                               (fs/file ".." "resources" "models_to_test.txt"))
    (mapv comm/write-dij-model model-records-keep)
    (if ini-return model-records-keep)))

(defn download-pipeline
  "Downloads files necessary for testing the models"
  [json-type options]
  (let [model-records-keep (initial-pipeline json-type options true)]
    (printf "Populating model folders for %d models \n" (count model-records-keep))
    (doall (map download/populate-model-folder model-records-keep))
    (printf "Downloading files (this could take some minutes) \n")
    (let [timed (download/my-time (doall (pmap download/download-into-model-folder model-records-keep)))]
      (printf "Total time taken: %s" (:iso timed)))))
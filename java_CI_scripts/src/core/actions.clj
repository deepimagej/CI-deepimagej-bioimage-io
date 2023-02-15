(ns core.actions
  "Define the steps for every action: init, download, and reproduce"
  (:require [config :refer [FILES]]
            [models]
            [summaries [summary :as summary] [reports :as reports]]
            [downloads [initial-checks :as initial-checks] [download :as download]]
            [reproduce [communicate :as comm] [run-fiji-scripts :as run-fiji-scripts]]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [babashka.process :as pr]))

(defn initial-pipeline
  "Creates the folders corresponding to test input json"
  [ini-return json-type options]
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
      ; txt input to numpy-tiff repo, needs that the :no-sample-images error is not checked during initial checks
      (comm/write-absolute-paths model-records-keep :rdf-path
                                 (fs/file ".." "numpy-tiff-deepimagej" "resources" "rdfs_to_test.txt")))
    (comm/write-absolute-paths model-records-keep :model-dir-path
                               (fs/file ".." "resources" "models_to_test.txt"))
    (mapv comm/write-dij-model model-records-keep)
    (if ini-return model-records-keep)))

(defn download-pipeline
  "Downloads files necessary for testing the models"
  [json-type options]
  (let [model-records-keep (initial-pipeline true json-type options)]
    (printf "Populating model folders for %d models \n" (count model-records-keep))
    (doall (map download/populate-model-folder model-records-keep))
    (printf "Downloading files (this could take some minutes) \n")
    (flush)
    (let [timed (download/my-time (doall (pmap download/download-into-model-folder model-records-keep)))]
      (printf "Total Time Taken: %s\n" (:iso timed)))))

;todo: generate test summaries for tested models that produce image
(defn reproduce-pipeline
 "For the linux case, where reproduce.run-fiji-scripts fails"
  [& _]
  (if (str/includes? (System/getProperty "os.name") "Windows")
    (run-fiji-scripts/-main)
    (let [_ (run-fiji-scripts/build-bash-script (:bash-script FILES))
          timed (download/my-time (pr/shell "sh" (:bash-script FILES)))]
      (printf "Total Time Taken: %s\n" (:iso timed))
      (flush)))
  (reports/basic-report))
(ns core
  (:require [summaries :refer [create-summa-dir]]
            [collection :refer [get-rdfs-to-test file-json->vector]]
            [models :refer [create-model-dir]]))

; TODO command line options to make main more useful
; -t run tests
; -j file json
; -s string json
; -m create model folders (download files)
; --summary write test summaries
(defn -main
  "Creates the folders corresponding to test input json (two models)"
  []
  (let [file "./pending_matrix/two_models.json"
        rdfs (get-rdfs-to-test (file-json->vector file))]
    (do
      (println "create dirs for test summaries")
      (mapv create-summa-dir rdfs)
      (println "creating dirs for models")
      (mapv create-model-dir rdfs))))
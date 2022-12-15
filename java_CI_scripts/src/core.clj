(ns core
  (:require [summaries :refer [create-summa-dir]]
            [collection :refer [get-rdfs-to-test file-json->vector]]))

; TODO command line options to make main more useful
(defn -main
  "Creates the folders corresponding to test input json (two models)"
  []
  (let [file "./pending_matrix/two_models.json"
        rdfs (get-rdfs-to-test (file-json->vector file))]
    (mapv create-summa-dir rdfs)))
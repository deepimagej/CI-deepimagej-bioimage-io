(ns models
  (:require [babashka.fs :as fs]
            [clj-yaml.core :as yaml]))

(defn parse-model
  "Takes the path of an rdf.yaml and parses it into an ordered dictionary"
  [rdf-path]
  (let [yaml-str (slurp (fs/file rdf-path))]
    (yaml/parse-string yaml-str)))

; fields of model:
;- rdf-path
;- summa-path


(defn build-model-folder
  "Downloads necessary files for testing in a directory"
  [rdf-path dir-name])
; download files (build folder for deepimagej)
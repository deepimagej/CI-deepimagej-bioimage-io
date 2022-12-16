(ns models
  (:require [babashka.fs :as fs]
            [clj-yaml.core :as yaml]))

(defn parse-model
  "Takes the path of an rdf.yaml and parses it into an ordered dictionary"
  [rdf-path]
  (let [yaml-str (slurp (fs/file rdf-path))]
    (yaml/parse-string yaml-str)))

(def weight-names {:torchscript "Pytorch"
                   :tensorflow_saved_model_bundle "Tensorflow"})

; fields of model:
;- rdf-path
;- summa-path
;- model-folder-path

(defrecord ModelToTest [paths name sample-images processing axes tiles])

(defn build-arg
  "Builds the argument string needed for the DeepImageJ Run command")

(defn build-model-folder
  "Downloads necessary files for testing in a directory"
  [rdf-path dir-name])
; download files (build folder for deepimagej)
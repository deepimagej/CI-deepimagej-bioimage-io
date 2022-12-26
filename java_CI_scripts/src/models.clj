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
(def p-process-names{:preprocess :pre-p :postprocess :post-p} )

(defrecord Model [paths name weights tensors tiles p-process])
(defrecord Paths [rdf-path summa-path model-folder-path])
(defrecord Weight [type source])
(defrecord Tensor [type name axes sample])
(defrecord PProcess [type scripts])

(defn get-weight-info
  "Put relevant weight information in a record, given a parsed rdf.
  The field 'type' is 'nil' for unsupported weights"
  ([rdf-dict]
   (map #(get-weight-info % rdf-dict) (keys (:weights rdf-dict))))
  ([key rdf-dict]
   (->Weight (weight-names key) (get-in rdf-dict [:weights key :source]))))

(defn get-p-process-info
  "Gathers information about pre- and post-processing from the yaml file"
  ([rdf-dict]
   (let [p-process-dict (get-in rdf-dict [:config :deepimagej :prediction])]
     (map #(get-p-process-info % p-process-dict) (keys p-process-dict))))
  ([key p-process-dict]
   ; enhance? list the kwargs, not only keep the first (?)
   (->PProcess (p-process-names key) (:kwargs (first (key p-process-dict))))))

; todo Fix this
(defn get-tensor-info
  "Get relevant information about :inputs and :outputs"
  [in-or-out-key rdf-dict]
  (let [tensor-info-dict (first (in-or-out-key rdf-dict))
        sample-key (in-or-out-key {:inputs :sample_inputs :outputs :sample_outputs})]
    (->Tensor (:name tensor-info-dict) (:axes tensor-info-dict) (first (sample-key rdf-dict)))))


(defn build-dij-arg
  "Builds the argument string needed for the DeepImageJ Run command"
  [])

(defn build-model-folder
  "Downloads necessary files for testing in a directory"
  [rdf-path dir-name])
; download files (build folder for deepimagej)
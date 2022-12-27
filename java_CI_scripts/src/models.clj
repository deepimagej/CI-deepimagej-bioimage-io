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
(def p*process-names{:preprocess :pre-p :postprocess :post-p} )

(defrecord Model [paths name nickname dij-config? weights tensors p*process tiles])
(defrecord Paths [rdf-path summa-path model-folder-path])
(defrecord Weight [type source])
(defrecord Tensor [type name axes sample shape])
(defrecord PProcess [type script])

(defn get-paths-info
  "Gets the different paths the model uses"
  [rdf-path])

(defn get-weight-info
  "Put relevant weight information in a record, given a parsed rdf.
  The field 'type' is 'nil' for unsupported weights"
  ([rdf-dict]
   (map #(get-weight-info % rdf-dict) (keys (:weights rdf-dict))))
  ([keyw rdf-dict]
   (->Weight (weight-names keyw) (get-in rdf-dict [:weights keyw :source]))))

(defn get-p*process-info
  "Gathers information about pre- and post-processing from the yaml file"
  ([rdf-dict]
   (let [p*process-dict (get-in rdf-dict [:config :deepimagej :prediction])]
     (map #(get-p*process-info % p*process-dict) (keys p*process-dict))))
  ([keyw p-process-dict]
   ; enhance? list the scripts in kwargs, not only keep the first (?)
   (->PProcess (keyw p*process-names) (:kwargs (first (keyw p-process-dict))))))

(defn get-tensor-info
  "Get info about input/output tensors"
  ([rdf-dict]
   (map #(get-tensor-info % rdf-dict) ["inputs" "outputs"]))
  ([str-key rdf-dict]
   (let [keyw (keyword str-key)
         *nput-map (first (keyw rdf-dict))
         sample (first ((keyword (str "sample_" str-key)) rdf-dict))
         ;; *inputs* in deepimagej config are sometimes a map, sometimes a list with 1 map...
         shape (->> (get-in rdf-dict [:config :deepimagej :test_information keyw])
                    (conj []) flatten first :size)]
     (->Tensor keyw (:name *nput-map) (:axes *nput-map) sample shape))))



(defn build-dij-arg
  "Builds the argument string needed for the DeepImageJ Run command"
  [])

(defn build-model-folder
  "Downloads necessary files for testing in a directory"
  [rdf-path dir-name])
; download files (build folder for deepimagej)
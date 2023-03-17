(ns models
  "Define a Model record to hold the relevant data from a parsed rdf"
  (:require [config :refer [ROOTS]]
            utils
            [summaries.summary :as summary]
            [clj-yaml.core :as yaml]
            [babashka.fs :as fs]))

(defn parse-model
  "Takes the path of an rdf.yaml and parses it into an ordered dictionary"
  [rdf-path]
  (let [yaml-str (slurp (fs/file rdf-path))]
    (yaml/parse-string yaml-str)))

(def weight-names
  "Association of key=name of weights in the yaml, and val=name of weights in model record"
  {:torchscript "Pytorch"
   :tensorflow_saved_model_bundle "Tensorflow"
   :onnx "Onnx"
   :pytorch_script "Pytorch"
   })

(def p*process-names{:preprocess :pre-p :postprocess :post-p} )

(defrecord Model [paths name nickname dij-config? rdf-info weights tensors p*process])
(defrecord RdfInfo [type dij-config? run-mode attach])
(defrecord Paths [rdf-path summa-path model-dir-path samples-path])
(defrecord Weight [type source])
(defrecord Tensor [type name axes sample shape])
(defrecord PProcess [type script])

(defn get-rdf-info
  [rdf-dict]
  (map->RdfInfo {:type        (:type rdf-dict)
                 :dij-config? (contains? (:config rdf-dict) :deepimagej)
                 :run-mode    (:run_mode rdf-dict)
                 :attach      (get-in rdf-dict [:attachments :files])}))

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
         ;; *inputs* in config: deepimagej: are sometimes a map, sometimes a list with 1 map...
         shape (->> (get-in rdf-dict [:config :deepimagej :test_information keyw])
                    (conj []) flatten first :size)]
     (->Tensor keyw (:name *nput-map) (:axes *nput-map) sample shape))))

(defn gen-model-path
  "Gets the path corresponding to the model directory of an rdf-path"
  ([coll-root model-root rdf-path] (utils/new-root-path coll-root model-root rdf-path))
  ([rdf-path] (gen-model-path (:collection-root ROOTS) (:models-root ROOTS) rdf-path)))

(defn gen-sample-path
  "Gets the path corresponding to the model directory of an rdf-path"
  ([coll-root sample-root rdf-path] (utils/new-root-path coll-root sample-root rdf-path))
  ([rdf-path] (gen-model-path (:collection-root ROOTS) (:samples-root ROOTS) rdf-path)))

(defn create-model-dir
  "Creates directory for the model given the path to an rdf"
  ([coll-root model-root rdf-path]
   (fs/create-dirs (gen-model-path coll-root model-root rdf-path)))
  ([rdf-path]
   (create-model-dir (:collection-root ROOTS) (:models-root ROOTS) rdf-path)))

(defn get-paths-info
  "Gets the different paths the model uses"
  [rdf-path]
  (->Paths rdf-path (summary/gen-summa-path rdf-path) (gen-model-path rdf-path) (gen-sample-path rdf-path)))

(defn build-model
  "Generates a Model record, data structure with the information needed for testing a model"
  [rdf-path]
  (let [rdf-dict (parse-model rdf-path)]
    (map->Model {:name      (:name rdf-dict)
                 :nickname  (get-in rdf-dict [:config :bioimageio :nickname])
                 :rdf-info  (get-rdf-info rdf-dict)
                 :paths     (get-paths-info (fs/path rdf-path))
                 :weights   (get-weight-info rdf-dict)
                 :tensors   (get-tensor-info rdf-dict)
                 :p*process (get-p*process-info rdf-dict)})))

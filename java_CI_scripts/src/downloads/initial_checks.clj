(ns downloads.initial-checks
  "Initial checks to see failed models before downloads and inference"
  (:require [clojure.set :refer [rename-keys]]
            [summaries.errors :refer [initial-errors]]))

; Input for these checks needs the model record and the parsed rdf
(defrecord ModelRP [model-record parsed-rdf])

; todo remove and use map->ModelRP
(defn build-model-rp
  "Given a model record, and a parsed-rdf, returns the model-rp dictionary"
  [model-record parsed-rdf]
  (hash-map :model-record model-record :parsed-rdf parsed-rdf))

(defn separate-by
 "Discriminative function should return true to keep testing, false if error occurred
 Data structure: {:keep-testing [list of model-rp] :error-found {:error-key1 [list of model-rp]} :error-key2 [list of model-rp]}"
  [[error-key disc-function] models-rp]
  (rename-keys (group-by disc-function models-rp) {true :keep-testing false error-key}))

(defn dij-config?
  "Checks whether a model-rp has deepimagej config (checks the record)"
  [model-rp]
  (get-in model-rp [:model-record :dij-config?]))

(defn no-run-mode?
  "Checks if the rdf does not have a run_mode key (or if is empty of found)"
  [model-rp]
  (let [run-mode-value (get-in model-rp [:parsed-rdf :run_mode])]
    (or (nil? run-mode-value) (not= (:name run-mode-value) "deepimagej"))))

;todo remove and use generic approach
(defn separate-by-dij-config
  "Separates models into the ones that have or not-have deepimagej config field in the rdf"
  [models]
  (rename-keys (group-by :dij-config? models) {true :keep-testing false :no-dij-config}))

(defn any-compatible-weight?
  "Tells if a model has any compatible weights (checks model record)"
  [model-rp])

(defn available-sample-images?
  "Tells if input and output tiff sample images are in the rdf.yaml"
  [])

(def error-functions
  "Associate discrimination function to each possible initial error"
  {:no-dij-config         dij-config?
   :no-sample-images      #(true)                           ;temporary
   :no-compatible-weights #(true)                           ;temporary
   :key-run-mode          no-run-mode?
   })

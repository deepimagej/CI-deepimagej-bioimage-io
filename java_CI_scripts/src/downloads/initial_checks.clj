(ns downloads.initial-checks
  "Initial checks to see failed models before downloads and inference"
  (:require [summaries.errors :refer [initial-errors]]))

; Input for these checks needs the model record and the parsed rdf
(defrecord ModelRP [model-record parsed-rdf])

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
  "DEPRECATED Separates models into the ones that have or not-have deepimagej config field in the rdf"
  [models]
  (clojure.set/rename-keys (group-by :dij-config? models) {true :keep-testing false :no-dij-config}))

(defn any-compatible-weight?
  "Tells if a model has any compatible weights (checks model record)"
  [model-rp])

(defn available-sample-images?
  "Tells if input and output tiff sample images are in the rdf.yaml"
  [])

(def error-functions
  "Associate discrimination function to each possible initial error"
  {:no-dij-config         dij-config?
   :no-sample-images      (fn [_] true)                           ;temporary
   :no-compatible-weights (fn [_] true)                           ;temporary
   :key-run-mode          no-run-mode?
   })

;  DATA STRUCTURE FOR DISCRIMINATED MODELS
(comment
  {:keep-testing [list-of-model-rp]
   :error-found  {:error-key1 [list-of-model-rp]
                  :error-key2 [list-of-model-rp]}})

(defn check-error
  "Adds results of checking a new error to the data structure for discriminated models.
  To be used as reducing function when iterating over all possible errors"
  [discriminated-models [error-key discriminating-fn]]
  (let [{to-keep true, with-error false} (group-by discriminating-fn (:keep-testing discriminated-models))]
    (hash-map :keep-testing to-keep
              :error-found (assoc (:error-found discriminated-models) error-key with-error))))

(defn separate-by-error
  "Discriminative function should return true to keep testing, false if error occurred
  Data structure: {:keep-testing [list of model-rp] :error-found {:error-key1 [list of model-rp]} :error-key2 [list of model-rp]}}"
  [error-fns models-rp-list]
  ;old implementation (rename-keys (group-by disc-function models-rp-list) {true :keep-testing false error-key})
  (reduce check-error {:keep-testing models-rp-list} error-functions)
  )
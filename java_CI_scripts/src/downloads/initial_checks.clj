(ns downloads.initial-checks
  (:require [clojure.set :refer [rename-keys]]))

; Initial checks to the models to see "fails" before inference and prevent downloads
; Input = model-rp: a dictionary of {:model-record <> : parsed-rdf} for each model
; - no deepimagej config
; - not available sample images
; - no compatible weights
; - not available specified p*processing
; - key run_mode exists (?) needs complete parsed-rdf
; - key format_version compatible (?) needs complete parsed-rdf

(defn build-model-rp
  "Given a model record, and a parsed-rdf, returns the model-rp dictionary"
  [model-record parsed-rdf]
  (hash-map :model-record model-record :parsed-rdf parsed-rdf))

(defn separate-by
 "Discriminative function should return true to keep testing, false if error occurred"
  [[error-key disc-function] models-rp]
  (rename-keys (group-by disc-function models-rp) {true :keep-testing false error-key}))

(defn dij-config?
  "Checks whether a model-rp has deepimagej config (checks the record)"
  [model-rp]
  (get-in model-rp [:model-record :dij-config?]))

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

(def possible-initial-errors
  {:no-dij-config {:fun dij-config? :msg "rdf does not have keys for config: deepimagej:"}
   })

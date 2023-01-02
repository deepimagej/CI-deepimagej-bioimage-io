(ns downloads
  (:require [clojure.set :refer [rename-keys]]))

; Initial checks to the models to see "fails" before inference and prevent downloads
; - no deepimagej config
; - not available sample images
; - no compatible weights

(defn separate-by-dij-config
  "Separates models into the ones that have or not-have deepimagej config field in the rdf"
  [models]
  (rename-keys (group-by :dij-config? models) {true :keep-testing false :no-dij-config}))

(defn all-incompatible-weights?
  "Tells if a model has only incompatible weights"
  [weights])

(defn available-sample-images?
  "Tells if input and output tiff sample images are in the rdf.yaml"
  [])



(defn populate-model-folder
  "Downloads in a directory the necessary files for testing a dij-compatible model"
  [model])
;todo pmap to potentially download in paralell (??)

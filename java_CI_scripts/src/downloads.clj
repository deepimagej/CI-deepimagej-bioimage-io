(ns downloads
  (:require [clojure.set :refer [rename-keys]]))

; Initial checks to the models to see easy "fails" and prevent downloads

(defn separate-by-dij-config
  "Separates models into the ones that have or not-have deepimagej config field in the rdf"
  [models]
  (rename-keys (group-by :dij-config? models) {true :keep-testing false :no-dij-config}))

(defn all-incompatible-weights?
  "Tells if a model has only incompatible weights"
  [weights])

(defn available-sample-images?
  "Tells if input and output tiff sample images are available"
  [])

; todo use (format)
(defn build-dij-arg
  "Builds the argument string needed for the DeepImageJ Run command"
  [model])


(defn populate-model-folder
  "Downloads in a directory the necessary files for testing a dij-compatible model"
  [model])
;todo pmap to potentially download in paralell


; todo check models that fail by default
; - no deepimagej config
; - no compatible weights
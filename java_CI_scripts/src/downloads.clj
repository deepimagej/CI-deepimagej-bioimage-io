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

; TODO fix p*processing macros after download (comment problematic lines)

(defmacro my-time
  "Variation on clojure.core/time: https://github.com/clojure/clojure/blob/clojure-1.10.1/src/clj/clojure/core.clj#L3884
  This macro returns a map with the time taken and the return value of the expression.
  Useful when timing side effects, no further composition is not usually needed (but still possible)"
  [expr]
  `(let [start# (java.time.Instant/ofEpochMilli (System/currentTimeMillis))
         ret# ~expr ;; evaluates the argument expression
         end# (java.time.Instant/ofEpochMilli (System/currentTimeMillis))
         duration# (java.time.Duration/between start# end#)]
     (hash-map :duration duration# :iso (str duration#)  :return ret#)))

(ns summaries.errors
  "Error keys and messages for the different possible failed test summaries")

(def initial-errors "Errors that could happen during the initial checks"
  {:no-dij-config         "rdf does not have keys for :config :deepimagej"
   :no-sample-images      "Sample image tiffs have not been generated from numpy tests files"
   :no-compatible-weights "rdf does not have a compatible weight format"
   :key-run-mode          "Test not yet available: rdf contains the key run_mode with value deepimagej"
   :no-p*process          "Needed p*processing file is not in the attachments (cannot be downloaded)"
   :not-a-model           "rdf type is not a DL model"      ; no test summaries for these though
   :incompatible-spec     "Version of the rdf is incompatible with DeepImageJ"})

; To add another initial error
; 1. add a key and error message to this dict
; 2. add a key and discrimination function in downloads.initial-checks


; errors can also happen:
; - while downloading [weights, attachments]
; not a priority, leave empty for now
(def download-errors "Errors that could happen while downloading files for testing"
  {})

(def reproduce-errors "Errors that could happen trying to reproduce output with DeepImageJ"
  {:dij-headless "Error while running DeepImageJ headless (CI did not produce an output image)"
   :comparison   "Difference between CI and expected outputs is greater than threshold (CI produced an output image)"
   })

(def all-errors (merge initial-errors download-errors reproduce-errors))

(def ci-stages "Different stages of the CI"
  {:initial   "Initial compatibility checks with DeepImageJ"
   :download  "Downloading testing resources for DeepImageJ"
   :reproduce "Reproduce test outputs with DeepImageJ headless"})

(def ci-stages-errors "different errors can be detected at different stages of the CI"
  {:initial   (set (keys initial-errors))
   :download  (set (keys download-errors))
   :reproduce (set (keys reproduce-errors))})

(defn find-stage
  "Gives the stage of an error key. If error key is not found, returns :initial by default"
  [error-k]
  (let [inverted (clojure.set/map-invert ci-stages-errors)
        check-fn (fn [[errors-set stage]] (if (contains? errors-set error-k) stage))
        found-stage (->> (map check-fn inverted) (filter some?) first)]
    (if (nil? found-stage) :initial found-stage)))
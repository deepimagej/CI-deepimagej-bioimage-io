(ns summaries.errors)

; possible initial errors
; - no deepimagej config
; - not available sample images
; - no compatible weights
; - not available specified p*processing
; - key run_mode exists (?) needs complete parsed-rdf
; - key format_version compatible (?) needs complete parsed-rdf

(def initial-errors
  {:no-dij-config         "rdf does not have keys for :config :deepimagej"
   :no-sample-images      "sample image tiffs have not been generated from numpy tests files"
   :no-compatible-weights "rdf does not have a compatible weight format"
   :key-run-mode          "rdf contains the key run_mode with value deepimagej"
   })

; To add another initial error
; 1. add a key and error message to this dict
; 2. add a key and discrimination function in downloads.initial-checks


; errors can also happen:
; - while downloading images
; - while downloading weights
; - while running dij headless (e.g. a preprocess macro)
; - test output != expected output
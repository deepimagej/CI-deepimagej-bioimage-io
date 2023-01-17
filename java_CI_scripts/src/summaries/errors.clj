(ns summaries.errors "Error keys and messages for the different possible failed test summaries")

; possible initial errors

; - key format_version compatible (?) needs complete parsed-rdf

(def initial-errors
  {:no-dij-config         "rdf does not have keys for :config :deepimagej"
   :no-sample-images      "sample image tiffs have not been generated from numpy tests files"
   :no-compatible-weights "rdf does not have a compatible weight format"
   :key-run-mode          "rdf contains the key run_mode with value deepimagej"
   :no-p*process          "no needed p*processing file in the attachments"
   })

; To add another initial error
; 1. add a key and error message to this dict
; 2. add a key and discrimination function in downloads.initial-checks



; errors can also happen:
; - while downloading [weights, attachments]


(def reproduce-errors "errors that happen trying to reproduce output with deepimagej"
  {:dij-headless "Error while running deepimagej headless (CI did not produce an output image)"
   :comparison   "CI output and Expected output are different (CI produced an output image)"
   })
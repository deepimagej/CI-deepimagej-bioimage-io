(ns downloads)


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
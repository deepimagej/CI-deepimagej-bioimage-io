(ns summaries.reproduce-metrics
  "Summaries form the output metric files produced after the inference with deepimagej run"
  (:require [config :refer [CONSTANTS]]
            [clojure.edn :as edn]
            [babashka.fs :as fs]))

(defn get-metrics-file
  "Returns the file of the output_metrics corresponding to the model record"
  [model-record]
  (fs/file (get-in model-record [:paths :model-dir-path]) (:output-metrics-name CONSTANTS)))

(defn get-output-metrics
  "Read into a hash-map the output metrics file generated after inference"
  [model-record]
  (edn/read-string (slurp (get-metrics-file model-record))))
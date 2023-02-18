(ns summaries.reproduce-checks
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

(defn metrics-produced?
  "Checks if headless run of deepimagej produced an image (metrics file is an empty map)"
  [model-record]
  (not (empty? (get-output-metrics model-record))))

(defn ok-metrics?
  "Check if CI output and expected output are sufficiently similar"
  [model-record]
  (> (:mse-threshold CONSTANTS) (get (get-output-metrics model-record) :mse 3.0)))

(def errors-fns
  {:dij-headless metrics-produced?
   :comparison   ok-metrics?})
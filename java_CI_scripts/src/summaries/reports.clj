(ns summaries.reports
  (:require [config :refer [FILES]]
            [reproduce.run-fiji-scripts :as run-fiji-scripts]
            [clojure.edn :as edn]
            [babashka.fs :as fs]))

(defn glob-models
  "globs a pattern on the models folder"
  [pattern]
  (map fs/file (fs/glob (:models-root FILES) pattern)))

(def result-files
  {:metrics  (glob-models "**/output_metrics.edn")
   :dij-args (glob-models "**/dij_args.edn")})

(defn basic-model-info
  [metrics-file dij-file]
  (let [{:keys [name nickname]} (edn/read-string (slurp dij-file))
        {:keys [mse mae]} (edn/read-string (slurp metrics-file))]
    (format "# %s\n- (%s) \n- mse: %.5f \n- mae: %.5f\n\n" name nickname mse mae)))

(defn basic-report
  "print basic report with results of CI"
  ([]
   (spit (:report FILES) "")
   (basic-report (:report FILES)))
  ([log-file]
   (doall (map #(let [msg (basic-model-info %1 %2)]
                  (run-fiji-scripts/print-and-log msg log-file))
               (:metrics result-files) (:dij-args result-files)))))
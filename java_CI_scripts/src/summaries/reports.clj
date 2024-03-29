(ns summaries.reports
  (:require [config :refer [ROOTS FILES CONSTANTS]]
            utils
            [reproduce.run-fiji-scripts :as run-fiji-scripts]
            [clojure.edn :as edn]
            [babashka.fs :as fs]))

(defn glob-models
  "globs a pattern on the models folder"
  [pattern]
  (map fs/file (fs/glob (:models-root ROOTS) pattern)))

(comment "fails if no models folder exists..."
  (def result-files
    {:metrics  (glob-models "**/output_metrics.edn")
     :dij-args (glob-models "**/dij_args.edn")}))

(defn basic-model-info
  [metrics-file dij-file]
  (let [{:keys [name nickname model-folder]} (edn/read-string (slurp dij-file))
        {:keys [mse mae max_val]} (edn/read-string (slurp metrics-file))]
    (format "# %s\n%s\n- (%s) \n- mse: %.5f \n- mae: %.5f\n- max_val: %.5f\n\n"
            name model-folder nickname mse mae max_val)))

(defn basic-report
  "print basic report with results of CI"
  ([]
   (spit (:report FILES) "" :append true)
   (basic-report (:report FILES)))
  ([log-file]
   (doall (map #(let [msg (basic-model-info %1 %2)]
                  (utils/print-and-log msg log-file))
               (glob-models "**/output_metrics.edn")
               (glob-models "**/dij_args.edn")))
   (printf "Report written in: %s\n" log-file)
   (flush)))
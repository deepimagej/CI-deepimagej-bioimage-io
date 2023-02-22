(ns summaries.summary
  (:require [config :refer [ROOTS FILES CONSTANTS]]
            utils
            [summaries.errors :as errors]
            [clj-yaml.core :as yaml]
            [babashka.fs :as fs]))

(defn gen-summa-path
  "Gets the path corresponding to the test summary of an rdf-path"
  ([coll-root summa-root rdf-path] (utils/new-root-path coll-root summa-root rdf-path))
  ([rdf-path] (gen-summa-path (:collection-root ROOTS) (:summa-root ROOTS) rdf-path)))

(defn create-summa-dir
  "Creates directory for resulting test summaries given the path to an rdf"
  ([coll-root summa-root rdf-path]
   (fs/create-dirs (gen-summa-path coll-root summa-root rdf-path)))
  ([rdf-path]
   (create-summa-dir (:collection-root ROOTS) (:summa-root ROOTS) rdf-path)))

(def default-summa-dict
  {:bioimageio_spec_version "0.4.8post1"
   ; :bioimageio_core_version "0.5.7" ; Finn suggested that the core version is not needed
   })

(defn gen-summa-dict
  "Add additional fields to default summary dictionary to generate a valid test summary
  Without args, generates summary for passed model"
  ([] (assoc default-summa-dict :status "passed" :name (:reproduce errors/ci-stages)))
  ([error-k]
   (let [stage (errors/find-stage error-k)]
     (assoc default-summa-dict :error (get errors/all-errors error-k "Other error")
                               :status "failed"
                               :name (stage errors/ci-stages) ))))

(defn write-test-summary!
  "Writes the yaml of the summary-dict in the summary path (in the model.paths)"
  [summa-dict model-record]
  (let [file-name (:summary-name CONSTANTS)
        yaml-str (yaml/generate-string summa-dict :dumper-options {:flow-style :block})
        out-file (fs/file (get-in model-record [:paths :summa-path]) file-name)]
    (spit out-file yaml-str)))

(defn write-summaries-from-error!
  "Writes the test summaries for the models with errors (entry from a discriminated-models dictionary)"
  [[error-key model-records]]
  (let [summa-dict (gen-summa-dict error-key)]
    (mapv (partial write-test-summary! summa-dict) model-records)
    (utils/print-and-log (format "Created %d test summaries for the error key %s\n" (count model-records) error-key)
                         (:summa-readme FILES))))
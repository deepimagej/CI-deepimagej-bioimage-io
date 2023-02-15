(ns summaries.summary
  (:require [collection :refer [COLLECTION-ROOT]]
            [summaries.errors]
            [clj-yaml.core :as yaml]
            [babashka.fs :as fs]))

(def SUMMA-ROOT "Path to the test summaries" (fs/path ".." "test_summaries"))

(defn get-parent-components
  "Gets the (seq) of components of parent directory structure (from root) of a given rdf path"
  ([root path] (butlast (fs/components (fs/relativize root path))))
  ([rdf-path] (get-parent-components COLLECTION-ROOT rdf-path)))

(defn new-root-path
  "Returns a path with a new root"
  [old-root new-root path]
  (apply fs/path (conj (get-parent-components old-root path) new-root)))

(defn gen-summa-path
  "Gets the path corresponding to the test summary of an rdf-path"
  ([coll-root summa-root rdf-path] (new-root-path coll-root summa-root rdf-path))
  ([rdf-path] (gen-summa-path COLLECTION-ROOT SUMMA-ROOT rdf-path)))

(defn create-summa-dir
  "Creates directory for resulting test summaries given the path to an rdf"
  ([coll-root summa-root rdf-path]
   (fs/create-dirs (gen-summa-path coll-root summa-root rdf-path)))
  ([rdf-path]
   (create-summa-dir COLLECTION-ROOT SUMMA-ROOT rdf-path)))

(def default-summa-dict
  {:bioimageio_spec_version "0.4.8post1"
   ; :bioimageio_core_version "0.5.7" ; Finn suggested that the core version is not needed
   })

(defn gen-summa-dict
  "Add additional fields to default summary dictionary to generate a valid test summary
  Without args, generates summary for passed model"
  ([] (gen-summa-dict "passed" :reproduce))
  ([status-k name-k & error-k]
   (let [valid-names {:initial   "initial compatibility checks with deepimagej"
                      :download  "downloading testing resources for deepimagej"
                      :reproduce "reproduce test outputs with deepimagej headless"}
         valid-statuses #{"passed" "failed"}
         status (get valid-statuses status-k)
         name (get valid-names name-k)
         dict (if (empty? error-k)
                default-summa-dict
                (assoc default-summa-dict :error ((first error-k) summaries.errors/initial-errors)))]
     (assoc dict :status status :name name))))

(defn write-test-summary!
  "Writes the yaml of the summary-dict in the summary path (in the model.paths)"
  [model-record summa-dict]
  (let [file-name "test_summary.yaml"
        yaml-str (yaml/generate-string summa-dict :dumper-options {:flow-style :block})
        out-file (fs/file (get-in model-record [:paths :summa-path]) file-name)]
    (spit out-file yaml-str)))

(defn write-summaries-from-error!
  "Writes the test summaries for the models with errors (entry from a discriminated-models dictionary)"
  [[error-key model-records]]
  (let [summa-dict (gen-summa-dict "failed" :initial error-key)]
    (mapv #(write-test-summary! % summa-dict) model-records)
    (printf "Created %d test summaries for the error key %s\n" (count model-records) error-key))
  )
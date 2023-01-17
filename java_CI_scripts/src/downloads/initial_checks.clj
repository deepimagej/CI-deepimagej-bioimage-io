(ns downloads.initial-checks
  "Initial checks to see failed models before downloads and inference"
  (:require [downloads.download :only [get-url-filename]]
    [babashka.fs :as fs]
    [summaries.errors :refer [initial-errors]]))

; Input for these checks needs the model record and the parsed rdf
(defrecord ModelRP [model-record parsed-rdf])

(defn dij-config?
  "Checks whether a model-rp has deepimagej config (checks the record)"
  [model-rp]
  (get-in model-rp [:model-record :dij-config?]))

(defn no-run-mode?
  "Checks if the rdf does not have a run_mode key (or if is empty of found)"
  [model-rp]
  (let [run-mode-value (get-in model-rp [:parsed-rdf :run_mode])]
    (or (nil? run-mode-value) (not= (:name run-mode-value) "deepimagej"))))

(defn any-compatible-weight?
  "Tells if a model has any compatible weights (checks model record)"
  [model-rp]
  (->> (get-in model-rp [:model-record :weights])
       (filter #(not= nil (:type %)))
       empty?
       not))

(defn available-sample-images?
  "Tells if input and output tiff sample images are in the local numpy_tiff folder"
  [model-rp]
  (let [samples-path (get-in model-rp [:model-record :paths :samples-path])]
    (and (fs/exists? samples-path) (= 4 (count (fs/list-dir samples-path))))))

(defn p*process-in-attachment?
  "Tells if the p*processing files needed are to be downloaded (are in the attachments)"
  [model-rp]
  (let [attach-list (get-in model-rp [:model-record :attach])
        attach-names (set (map downloads.download/get-url-filename attach-list))
        p*p-names (map :script (get-in model-rp [:model-record :p*process]))]
    (every? identity (map (partial contains? attach-names) p*p-names))))

(def error-functions
  "Association of each possible initial error with a discrimination function.
  Order of errors here affects order on how errors are checked"
  {:no-compatible-weights any-compatible-weight?
   :no-dij-config         dij-config?
   :no-sample-images      available-sample-images?
   :key-run-mode          no-run-mode?
   :no-p*process          (fn [_] true)
   })

;  DATA STRUCTURE FOR DISCRIMINATED MODELS
(comment
  {:keep-testing [list-of-model-rp]
   :error-found  {:error-key1 [list-of-model-rp]
                  :error-key2 [list-of-model-rp]}})

(defn check-error
  "Adds results of checking a new error to the data structure for discriminated models.
  To be used as reducing function when iterating over all possible errors"
  [discriminated-models [error-key discriminating-fn]]
  (let [{to-keep true, with-error false} (group-by discriminating-fn (:keep-testing discriminated-models))]
    (hash-map :keep-testing to-keep
              :error-found (assoc (:error-found discriminated-models) error-key with-error))))

(defn separate-by-error
  "Discriminative function should return true to keep testing, false if error occurred
  After an error happens, no more error checks are made for a model"
  ([models-rp-list]
   (separate-by-error models-rp-list error-functions))
  ([models-rp-list error-fns]
   (reduce check-error {:keep-testing models-rp-list} error-fns)))

; Deprecated, but not deleted. It can clarify how things worked initially
(defn separate-by-dij-config
  "DEPRECATED Separates models into the ones that have or not-have deepimagej config field in the rdf.
  Deprecated by using generic approach to all error types"
  [models]
  (clojure.set/rename-keys (group-by :dij-config? models) {true :keep-testing false :no-dij-config}))
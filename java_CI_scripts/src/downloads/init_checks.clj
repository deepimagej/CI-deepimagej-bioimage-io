(ns downloads.init-checks
  "Initial checks to see failed models before downloads and inference"
  (:require [downloads.download :only [get-url-filename]]
            [downloads.p-process :only [get-p*process-names]]
            [babashka.fs :as fs]
            [summaries.errors :as errors]))

(defn model?
  "Checks if a model record is from an rdf of type 'model'"
  [model-record]
  (= "model" (get-in model-record [:rdf-info :type])))

(defn dij-config?
  "Checks whether a model-record has deepimagej config"
  [model-record]
  (get-in model-record [:rdf-info :dij-config?]))

(defn no-run-mode?
  "Checks if the rdf does not have a run_mode key (or if is empty of found)"
  [model-record]
  (let [run-mode-value (get-in model-record [:rdf-info :run-mode])]
    (or (nil? run-mode-value) (not= (:name run-mode-value) "deepimagej"))))

(defn any-compatible-weight?
  "Tells if a model has any compatible weights (checks model record)"
  [model-record]
  (->> (:weights model-record)
       (filter #(not= nil (:type %)))
       empty?
       not))

(defn available-sample-images?
  "Tells if input and output tiff sample images are in the local numpy_tiff folder"
  [model-record]
  (let [samples-path (get-in model-record [:paths :samples-path])]
    (and (fs/exists? samples-path) (= 4 (count (fs/list-dir samples-path))))))

(defn p*process-in-attachment?
  "Tells if the p*processing files needed are to be downloaded (are in the attachments)"
  [model-record]
  (let [attach-list (get-in model-record [:rdf-info :attach])
        attach-names (set (map downloads.download/get-url-filename attach-list))
        p*p-names (downloads.p-process/get-p*process-names model-record)]
    (every? (partial contains? attach-names) p*p-names)))

(def error-functions
  "Association of each possible initial error with a discrimination function.
  Order of errors here affects order on how errors are checked"
  {:key-run-mode          no-run-mode?
   :no-compatible-weights any-compatible-weight?
   :no-dij-config         dij-config?
   :no-sample-images      available-sample-images?
   :no-p*process          p*process-in-attachment?})

;  DATA STRUCTURE FOR DISCRIMINATED MODELS
(comment
  {:keep-testing [list-of-model-rp]
   :error-found  {:error-key1 [list-of-model-rp]
                  :error-key2 [list-of-model-rp]}})

; todo, put this somewhere different, used also for final-checks
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
  ([models-list]
   (separate-by-error models-list error-functions))
  ([models-list error-fns]
   (reduce check-error {:keep-testing models-list} error-fns)))

; Deprecated, but not deleted. It can clarify how things worked initially
(defn separate-by-dij-config
  "DEPRECATED Separates models into the ones that have or not-have deepimagej config field in the rdf.
  Deprecated by using generic approach to all error types"
  [models]
  (clojure.set/rename-keys (group-by :dij-config? (map :rdf-info models)) {true :keep-testing false :no-dij-config}))
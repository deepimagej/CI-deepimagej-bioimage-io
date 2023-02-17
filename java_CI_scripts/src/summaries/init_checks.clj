(ns summaries.init-checks
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

(def errors-fns
  "Association of each possible initial error with a discrimination function.
  Order of errors here affects order on how errors are checked"
  {:key-run-mode          no-run-mode?
   :no-compatible-weights any-compatible-weight?
   :no-dij-config         dij-config?
   :no-sample-images      available-sample-images?
   :no-p*process          p*process-in-attachment?})

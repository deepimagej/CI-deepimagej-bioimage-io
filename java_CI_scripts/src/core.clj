(ns core
  (:require [summaries :refer [create-summa-dir gen-summa-dict write-test-summary]]
            [collection :refer [get-rdfs-to-test file-json->vector str-json->vector]]
            [models :refer [create-model-dir build-model]]
            [downloads :refer [separate-by-dij-config]]
            [reproduce.communicate :refer [build-dij-model write-comm-file]]
            collection-test models-test summaries-test downloads-test reproduce.communicate-test
            (clojure [string :as str]
                     [test :refer [run-tests]])
            [clojure.tools.cli :as cli]
            [babashka.fs :as fs]
            [cheshire.core :as json]))

(defn valid-json? [s]
  (try (json/parse-string s)
       (catch Exception e false))) ;JsonParseException

; TODO actions
; initial: creates folders and test summaries for incompatible (avoid downloads)
; download: populates model folders (downloads files)
; reproduce: write test summaries (from outputs of tested headless dij)
; -v --verbosity
(def cli-options
  [["-u" "--unit-test" "Run all unit tests"
    :default false]
   ["-j" "--json-file FILE" "Read input from json FILE"
    :default "./pending_matrix/two_models.json"
    :validate [#(fs/exists? %) "File must exist"
               #(valid-json? (slurp %)) "File must be valid json"]]
   ["-s" "--json-string STRING" "Read input from raw json STRING"
    ; not given a default value, to make it incompatible with -j
    :validate [#(valid-json? %) "String must be valid json"]]
   ;; A boolean option defaulting to nil
   ["-h" "--help" "Show help"]])

(defn usage [options-summary]
  (->> ["DeepImageJ CI of models from the BiomageModelZoo"
        ""
        "Usage: bb -m core [options] (in the java_CI_scripts working directory)"
        ""
        "Options:"
        options-summary
        "Please refer to the manual page for more information (TO-DO)."]
       (str/join \newline)))

(def error-title "The following errors occurred while parsing your command:\n\n")

(defn error-msg [errors]
  (str error-title (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      :else
      {:options options :action (first arguments)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;TODO heavy refactor on action and error-name
(defn initial-pipeline
  "Creates the folders corresponding to test input json"
  [json-type options]
  (let [parsing-function (json-type {:json-file   file-json->vector
                                     :json-string str-json->vector})
        rdfs (get-rdfs-to-test (parsing-function (json-type options)))
        model-records (map build-model rdfs)
        {:keys [no-dij-config keep-testing]} (separate-by-dij-config model-records)
        failed-dict (gen-summa-dict "failed" :initial :no-dij-config)]
    (do (println "Creating dirs for test summaries")
        (mapv create-summa-dir rdfs)
        (println "Creating dirs for models")
        (mapv create-model-dir rdfs)
        (println "Creating test summaries for" (count no-dij-config) "models")
        (mapv #(write-test-summary % failed-dict) no-dij-config)
        (println "Creating comm file for" (count keep-testing) "models")
        (write-comm-file (map build-dij-model keep-testing)))))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (cond
        (:unit-test options)
        ;core cannot run the tests from 'core-test due to cyclic load dependency!!
        (run-tests 'collection-test 'summaries-test 'models-test 'downloads-test 'reproduce.communicate-test)
        (:json-string options)
        (initial-pipeline :json-string options)
        :else
        (initial-pipeline :json-file options)))))
(ns core
  (:require [summaries :refer [create-summa-dir]]
            [collection :refer [get-rdfs-to-test file-json->vector str-json->vector]]
            [models :refer [create-model-dir]]
            [collection-test] [models-test] [summaries-test] [downloads-test]
            (clojure [string :as str]
                     [test :refer [run-tests]])
            [clojure.tools.cli :as cli]
            [babashka.fs :as fs]
            [cheshire.core :as json]))

(defn valid-json? [s]
  (try (json/parse-string s)
       (catch Exception e false))) ;JsonParseException

; '{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"}, {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}'
(def a-valid-json "{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"},
          {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}")

; TODO command line options to make main more useful
; -p --pre-test (avoid downloads)
; -m --model-folders populate model folders (also downloads files)
; -t --test-summaries write test summaries
; -v --verbosity
(def cli-options
  [["-u" "--unit-test" "Run all unit tests"
    :default false]
   ["-j" "--json-file FILE" "Read input from json FILE"
    :default "./pending_matrix/two_models.json"
    :validate [#(fs/exists? %) "File must exist"]]
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

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

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
      :else ; failed custom validation => exit with usage summary
      {:options options :action (first arguments)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn create-dirs
  "Creates the folders corresponding to test input json"
  [json-type options]
  (let [parsing-function (json-type {:json-file   file-json->vector
                                     :json-string str-json->vector})
        rdfs (get-rdfs-to-test (parsing-function (json-type options)))]
    (do
      (println "create dirs for test summaries")
      (mapv create-summa-dir rdfs)
      (println "creating dirs for models")
      (mapv create-model-dir rdfs))))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (do (println "The Options \n" options)
          (println "The Action \n" action)
          (cond
            (:unit-test options)
            (run-tests 'collection-test 'summaries-test 'models-test 'downloads-test)
            (:json-string options)
            (create-dirs :json-string options)
            :else
            (create-dirs :json-file options))))))
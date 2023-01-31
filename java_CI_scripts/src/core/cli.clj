(ns core.cli
  (:require [core.actions]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [cheshire.core :as json]
            [babashka.fs :as fs]))

(def actions-fns "Association between actions and function that implements them"
  {"init"     (partial core.actions/initial-pipeline false)
   "download"  core.actions/download-pipeline
   "reproduce" core.actions/reproduce-pipeline})

(defn valid-json? [s]
  (try (json/parse-string s)
       (catch Exception e false))) ;JsonParseException
; todo? multimethod, dispatch on string content

(def cli-options
  [["-u" "--unit-test" "Run all unit tests"
    :default false]
   ["-j" "--json-file FILE" "Read input from json FILE"
    :default "./pending_matrix/use_cases.json"
    :validate [#(fs/exists? %) "File must exist"
               #(valid-json? (slurp %)) "File must contain valid json"]]
   ["-s" "--json-string STRING" "Read input from raw json STRING"
    ; not given a default value, to make it incompatible with -j
    :validate [#(valid-json? %) "String must be valid json"]]
   ;; A boolean option defaulting to nil
   ["-h" "--help" "Show help"]])

(defn usage [options-summary]
  (->> ["DeepImageJ CI for models from the BioImage Model Zoo (https://bioimage.io/)"
        ""
        "Usage: bb -m core.main [options] [action] (in the java_CI_scripts working directory)"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        " init (DEFAULT) Initial checks & generate folder structures and files for the compatible models to test."
        " download       Populate model folders (download files). Build args for DeepImagej headless."
        " reproduce      Run the models on Fiji with DeepImageJ headless. Create tests summaries (to-do)."
        ""
        "Please refer to the docs page for more information:
        https://github.com/ivan-ea/CI-deepimagej-bioimage-io/blob/master/java_CI_scripts/Readme.md"]
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
      ;; custom validation on arguments
      (= 0 (count arguments))
      {:options options :action "init"}
      (and (>= (count arguments) 1)
           ((set (keys actions-fns)) (first arguments)))
      {:options options :action (first arguments)}
      ; default behavior will be showing help message
      :else
      {:exit-message (usage summary) :ok? true})))

(defn exit [status msg]
  (println msg)
  (System/exit status))
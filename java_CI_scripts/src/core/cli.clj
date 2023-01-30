(ns core.cli
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [cheshire.core :as json]
            [babashka.fs :as fs]))


(defn valid-json? [s]
  (try (json/parse-string s)
       (catch Exception e false))) ;JsonParseException
; TODO? multimethod, dispatch on string content

; TODO? actions
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
               #(valid-json? (slurp %)) "File must contain valid json"]]
   ["-s" "--json-string STRING" "Read input from raw json STRING"
    ; not given a default value, to make it incompatible with -j
    :validate [#(valid-json? %) "String must be valid json"]]
   ;; A boolean option defaulting to nil
   ["-h" "--help" "Show help"]])

(defn usage [options-summary]
  (->> ["DeepImageJ CI for models from the Bioimage Model Zoo (https://bioimage.io/)"
        ""
        "Usage: bb -m core [options] (in the java_CI_scripts working directory)"
        ""
        "Options:"
        options-summary
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
      :else
      {:options options :action (first arguments)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))
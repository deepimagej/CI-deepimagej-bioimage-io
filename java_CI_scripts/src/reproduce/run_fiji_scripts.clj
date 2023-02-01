(ns reproduce.run-fiji-scripts
  "Run the 2 scripts: inference with DeepImageJ and comparison with Fiji.
  Use bb instead of bash for windows compatibility"
  (:require [config :refer [FILES CONSTANTS]] [babashka.fs :as fs]
            [babashka.process :as pr]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.set :as set]
            [clojure.string :as str]
            [downloads.download :as download]))

; pr/tokenize removes escaped quotes \" and single quotes (use the cmd vector, not the string)
; ON LINUX: java.shell and bb.process/sh fail to convey the fiji args correctly
; CAUTION: Fiji stores the last valid argument for the variables across executions!!

(def LOG-FILES {:out (fs/file ".." "test_summaries" "fiji_log_out.txt")
                :err (fs/file ".." "test_summaries" "fiji_log_err.txt")})
(def COMM-FILE (fs/file ".." "resources" "models_to_test.txt"))
(def FIJI-HOME (fs/file (System/getProperty "user.home") "blank_fiji" "Fiji.app"))
(def fiji-executable (str (first (fs/glob FIJI-HOME "ImageJ-*"))))
(def fiji-flags ["--headless" "--ij2" "--console" "--run"])
(def fiji-arg-name "folder")

(def script-names "Absolute paths to the scripts"
  (->> ["test_1_with_deepimagej.clj" "create_output_metrics.py"]
       (map #(fs/file "src" "reproduce" %))
       (map #(str (fs/absolutize %)))))

(def script-prints [(format "-- script 1/2: TESTING WITH DEEPIMAGEJ HEADLESS\n")
                    (format "-- script 2/2: COMPARING TO EXPECTED OUTPUT\n")])

(defn read-lines
  "Reads every line on a file, returns a vector of strings"
  [file]
  (with-open [rdr (io/reader file)]
    (into [] (line-seq rdr))))

(def model-folders (read-lines COMM-FILE))

(def messages
  {:start (format "STARTED TESTING THE %d MODELS WITH DEEPIMAGEJ IN FIJI\n\n"
                  (count model-folders))
   :end   (format "\nFINISHED TESTING THE %d MODELS IN FIJI\n\nLogs are in: %s and %s\n"
                  (count model-folders) (str (fs/absolutize (:out LOG-FILES)))
                  (fs/file-name (:err LOG-FILES)))})

(defn quote-arg
  "Quotes the argument to fiji script correctly (different in linux and windows)"
  [model-folder]
  (let [quotation_marks #{\" \'},
        outer (if (str/includes? (System/getProperty "os.name") "Windows") \" \')
        ;outer \" ;debug
        inner (first (set/difference quotation_marks #{outer}))]
    (str outer fiji-arg-name "=" inner model-folder inner outer)))

(defn compose-command
  "Creates a vector with the components of the command"
  [model-folder script-name]
  (as-> [fiji-executable] cmd-vec
        (into cmd-vec fiji-flags)
        (into cmd-vec [script-name (quote-arg model-folder)])))

(defn string-command
  "Builds the complete command string for a given script.
  Useful for debugging: paste partial command on terminal"
  [model-folder script-name]
  (str/join " " (compose-command model-folder script-name)))

(defn print-and-log
  "Prints a string message and logs it on all log files provided"
  ([msg] (apply print-and-log msg (vals LOG-FILES)))
  ([msg & log-files]
   (print msg)
   (flush)
   (mapv #(spit % msg :append true) log-files)))

(def execution-dict
  "Vector with info of the commands and prints to do at every step"
  (vec (map-indexed (fn [idx model-folder]
                      {:message  (format "- MODEL %d/%d\n" (inc idx) (count model-folders))
                       :cmd-vecs (mapv (partial compose-command model-folder) script-names)})
                    model-folders)))
; can be access it with (get-in execution-dict [0 :cmd-vecs 0]), but not needed

; NOTE: pr/sh or pr/shell escapes quotes incorrectly
; shell/sh does it correctly but only on Windows...
(defn run-exec-step
  "Perform the commands for 1 execution step (1 model, 2 scripts)"
  [{:keys [message cmd-vecs]}]
  (print-and-log message)
  (mapv (fn [cmd msg] (let [return (apply shell/sh cmd)]
                        (print-and-log msg)
                        ;(print-and-log (:err return) (:err LOG-FILES)) ; print errors on stdout?
                        (spit (:err LOG-FILES) (:err return) :append true)
                        (print-and-log (:out return) (:out LOG-FILES))))
        cmd-vecs script-prints))

(defn -main []
  "Runs the commands from the execution-dict. Logs outputs"
  (mapv #(spit % "") (vals LOG-FILES))
  (print-and-log (:start messages))
  (let [timed (download/my-time (mapv run-exec-step execution-dict))]
    (print-and-log (:end messages))
    (print-and-log (format "Total Time Taken: %s\n" (:iso timed)))))

;; Create bash file automatically

(defn write-bash
  "writes string in a bash script"
  ([string] (write-bash (:bash-script FILES) string))
  ([bash-file string]
   (spit bash-file string :append true)))

(defn bash-and-log
  "Returns the string corresponding to the bash command that prints and redirects stdout, and redirecting stderr"
  ([cmd] (bash-and-log cmd (:out LOG-FILES) (:err LOG-FILES)))
  ([cmd log-out log-err]
   (str cmd " 2>> " (fs/absolutize log-err) " | tee -a " (fs/absolutize log-out) "\n\n")))

(defn echo-and-log
  "String corresponding to the bash command that echoes a message and logs in files"
  ([msg] (apply echo-and-log msg (vals LOG-FILES)))
  ([msg & log-files]
   (str "echo \"" msg "\"" " | tee -a " (str/join " " (map fs/absolutize log-files)) "\n\n")))

(defn bash-exec-step
  "Generates the bash commands for 1 execution step (1 model, 2 fiji scripts)"
  [{:keys [message cmd-vecs]}]
  (write-bash (echo-and-log message))
  (mapv (fn [cmd msg]
          (write-bash (echo-and-log msg))
          (write-bash (bash-and-log (str/join " " cmd))))
        cmd-vecs script-prints))

(defn build-bash-script
  ([] (build-bash-script (:bash-script FILES)))
  ([bash-file]
   (spit bash-file "#! /usr/bin/env sh\n\n")
   (write-bash "# This file was generated automatically by run_fiji_scripts.clj\n")
   (write-bash "# This is needed in Linux for Fiji to run correctly\n\n")
   (mapv #(write-bash (str "echo \"\" > " (fs/absolutize %) "\n\n")) (vals LOG-FILES))
   (write-bash (echo-and-log (:start messages)))
   (mapv bash-exec-step execution-dict)
   (write-bash (echo-and-log (:end messages)))
   (printf "Bash script with %d lines of code written in: %s\n"
           (count (str/split-lines (slurp bash-file))) (str (fs/absolutize bash-file)))))
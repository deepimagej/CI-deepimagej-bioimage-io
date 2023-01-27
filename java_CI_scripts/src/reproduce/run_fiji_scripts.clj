(ns reproduce.run-fiji-scripts
  "Run the 2 scripts: inference with DeepImageJ and comparison with Fiji.
  Use bb instead of bash for windows compatibility"
  (:require [babashka.fs :as fs]
            [babashka.process :as pr]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.set :as set]
            [clojure.string :as str]
            [downloads.download :refer [my-time]]))

; pr/tokenize removes escaped quotes \" and single quotes (use the cmd vector, not the string)
; ON LINUX: java.shell and bb.process/sh fail to convey the fiji args correctly
; CAUTION: Fiji stores the last valid argument for the variables across executions!!

; TODO: 2 log files: for :out and :err
(def LOG-FILE (fs/file ".." "test_summaries" "complete_fiji_log.txt"))
(def COMM-FILE (fs/file ".." "resources" "models_to_test.txt"))
(def FIJI-HOME (fs/file (System/getProperty "user.home") "blank_fiji" "Fiji.app"))
(def BASH-FILE (fs/file ".." "resources" "models_to_test.sh"))
(def fiji-executable (str (first (fs/glob FIJI-HOME "ImageJ-*"))))
(def flags ["--headless" "--ij2" "--console" "--run"])
(def arg-name "folder")

(def script-names "Absolute paths to the scripts"
  (->> ["test_1_with_deepimagej.clj" "create_output_metrics.py"]
       (map #(fs/file "src" "reproduce" %))
       (map #(str (fs/absolutize %)))))

(def script-prints [(format "-- script 1/2: TESTING WITH DEEPIMAGEJ HEADLESS\n")
                    (format "-- script 2/2: COMPARING TO EXPECTED OUTPUT\n")])

;todo remove
(def script-data
  {:dij-headless {:name (first script-names) :msg (first script-prints)}
   :compare      {:name (second script-names) :msg (second script-prints)}})

(defn read-lines
  "Reads every line on a file, returns a vector of strings"
  [file]
  (with-open [rdr (io/reader file)]
    (into [] (line-seq rdr))))

(def model-folders (read-lines COMM-FILE))

(def messages
  {:start (format "STARTED TESTING THE %d MODELS WITH DEEPIMAGEJ IN FIJI\n\n" (count model-folders))
   :end   (format "\nFINISHED TESTING THE %d MODELS IN FIJI\n\nLogs are in: %s\n"
                  (count model-folders) (str (fs/absolutize LOG-FILE)))})

(defn quote-arg
  "Quotes the argument to fiji script correctly (different in linux and windows)"
  [model-folder]
  (let [quotation_marks #{\" \'},
        outer (if (str/includes? (System/getProperty "os.name") "Windows") \" \')
        ;outer \" ;debug
        inner (first (set/difference quotation_marks #{outer}))]
    (str outer arg-name "=" inner model-folder inner outer)))

(defn compose-command
  "Creates a vector with the components of the command"
  [model-folder script-name]
  (as-> [fiji-executable] cmd-vec
        (into cmd-vec flags)
        (into cmd-vec [script-name (quote-arg model-folder)])))

(defn string-command
  "Builds the complete command string for a given script.
  Useful for debugging: paste partial command on terminal"
  [model-folder script-name]
  (str/join " " (compose-command model-folder script-name)))

(defn print-and-log
  "Prints a string message and logs it on a file"
  ([msg] (print-and-log LOG-FILE msg))
  ([log-file msg]
   (print msg)
   (flush)
   (spit log-file msg :append true)))

(defn echo-and-log
  "Bash command corresponding to printing and logging a message"
  ([msg] (echo-and-log LOG-FILE msg))
  ([log-file msg]
   (str "echo " msg " | tee -a " (fs/absolutize log-file))))

(def execution-dict
  "Vector with info of the commands and prints to do at every step"
  (vec (map-indexed (fn [idx model-folder]
                      {:message  (format "- MODEL %d/%d\n" (inc idx) (count model-folders))
                       :cmd-vecs (mapv (partial compose-command model-folder) script-names)})
                    model-folders)))
; can be access it with (get-in execution-dict [0 :cmd-vecs 0]), but not needed

(defn run-exec-step
  "Perform the commands for 1 model. Keyword decides functions for :clj or :bash"
  ([execution-step] (run-exec-step execution-step :clj))
  ([{:keys [message cmd-vecs]} lang-k]
   (print-and-log message)
   (mapv (fn [s m] (let [return (apply pr/sh s)]
                     (print-and-log m)
                     ;todo log :err
                     (print-and-log (:out return))))
        cmd-vecs script-prints)))

(defn -main []
  "Runs the commands from the execution-dict. Logs outputs"
  (spit LOG-FILE "")
  (print-and-log (:start messages))
  (let [timed (my-time (mapv run-exec-step execution-dict))]
    (print-and-log (:end messages))
    (print-and-log (format "Total Time Taken: %s\n" (:iso timed)))))

; TODO: create bash file atomatically
; print and log with tee?
; ... 2> err.txt | tee -a out.txt

(defn build-bash-script
  [bash-file]
  (spit bash-file "#! /usr/bin/env sh\n\n"))
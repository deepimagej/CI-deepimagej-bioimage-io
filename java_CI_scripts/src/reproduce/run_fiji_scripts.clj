(ns reproduce.run-fiji-scripts
  "Run the 2 scripts: inference with DeepImageJ and comparison with Fiji.
  Use bb instead of bash for windows compatibility"
  (:require [downloads.download :refer [my-time]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.java.shell :as shell]
            [babashka.fs :as fs]
            [babashka.process :as pr]))
; pr/tokenize removes escaped quotes \" and single quotes
; ON LINUX: java.shell and bb.process/sh fail to convey the fiji args correctly

(def LOG-FILE (fs/file ".." "test_summaries" "complete_fiji_log.txt"))
(def COMM-FILE (fs/file ".." "resources" "models_to_test.txt"))
(def FIJI-HOME (fs/file (System/getProperty "user.home") "blank_fiji" "Fiji.app"))
(def fiji-executable (str (first (fs/glob FIJI-HOME "ImageJ-*"))))
(def flags ["--headless" "--ij2" "--console" "--run"])
(def arg-name "folder")

(def script-names "Absolute paths to the scripts"
  (->> ["test_1_with_deepimagej.clj" "create_output_metrics.py"]
       (map #(fs/file "src" "reproduce" %))
       (map #(str (fs/absolutize %)))))

(def script-prints [(format "- script 1/2: TESTING WITH DEEPIMAGEJ HEADLESS\n")
                    (format "- script 2/2: COMPARING TO EXPECTED OUTPUT\n")])

(def script-data
  {:dij-headless {:name (first script-names)  :msg (first script-prints)}
   :compare      {:name (second script-names) :msg (second script-prints)}})

(defn read-lines
  "Reads every line on a file, returns a vector of strings"
  [file]
  (with-open [rdr (io/reader file)]
    (into [] (line-seq rdr))))

(def model-folders (read-lines COMM-FILE))

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
  [script-name model-folder]
  (as-> [fiji-executable] cmd-vec
       (into cmd-vec flags)
        (into cmd-vec [script-name (quote-arg model-folder)])))

(defn string-command
  "Builds the complete command string for a given script.
  Useful for debugging: paste partial command on terminal"
  [script-name model-folder]
  (str/join " " (compose-command script-name model-folder)))

(defn run-command
  [script-name model-folder]
  (println "running the command:" (string-command script-name model-folder))
  (let [cmd-vec (compose-command script-name model-folder)]
    (if (str/includes? (System/getProperty "os.name") "Windows")
      (apply shell/sh cmd-vec)
      (apply pr/sh cmd-vec))))

(defn print-and-log
  "Prints a string message and logs it on a file"
  [log-file msg]
  (print msg)
  (flush)
  (spit log-file msg :append true))

(defn run-script-&-info
  [log-file total idx model-folder script-k]
  (let [m1 (format "- MODEL %d/%d\n" idx total)
        {{:keys [name msg]} script-k} script-data
        _ (mapv (partial print-and-log log-file) [m1 msg])
        output-map (run-command name model-folder)]
    (print-and-log log-file (:out output-map))))

(defn -main
  "Loops over models to test and the 2 scripts (inference and comparison)
  Prints info on screen and appends to log"
  ([] (-main LOG-FILE))
  ([log-path]
   (let [log-file (fs/file log-path)
         _ (spit log-file "")
         m-start (format "STARTED TESTING THE %d MODELS WITH DEEPIMAGEJ IN FIJI\n\n" (count model-folders))
         _ (print-and-log log-file m-start)
         run (partial run-script-&-info log-file (count model-folders))
         timed (my-time (doall (map-indexed (fn [i x]
                                              (run (inc i) x :dij-headless)
                                              (run (inc i) x :compare))
                                            model-folders)))
         m-final (format "FINISHED TESTING THE %d MODELS IN FIJI\nLogs are in: %s\nTotal time taken: %s\n"
                         (count model-folders) (str (fs/absolutize log-file)) (:iso timed))]
     (print-and-log log-file m-final)
     (System/exit 0))))


(ns reproduce.run-fiji-scripts
  "Run the 2 scripts: inference with DeepImageJ and comparison with Fiji.
  Use bb instead of bash for windows compatibility"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.java.shell :as shell]
            [babashka.fs :as fs]
            [babashka.process :as pr]))

; use shell instead of process due to weird escaping of quoted args \" \' ...

(def LOG-FILE (fs/file ".." "test_summaries" "complete_fiji_log.txt"))
(def COMM-FILE (fs/file ".." "resources" "models_to_test.txt"))
(def FIJI-HOME (fs/file (System/getProperty "user.home") "blank_fiji" "Fiji.App"))
(def fiji-executable (str (first (fs/glob FIJI-HOME "ImageJ*"))))
(def arg-name "folder")

(def script-names "Absolute paths to the scripts"
  (->> ["test_1_with_deepimagej.clj" "create_output_metrics.py"]
       (map #(fs/file "src" "reproduce" %))
       (map #(str (fs/absolutize %)))))

(def script-prints [(format "TESTING WITH DEEPIMAGEJ HEADLESS\n")
                    (format "COMPARING WITH EXPECTED OUTPUT\n")])

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
        inner (first (set/difference quotation_marks #{outer}))]
    (str outer arg-name "=" inner model-folder inner outer)))

(defn run-command
  "Builds the complete command string for a given script"
  [script-name model-folder]
  (shell/sh fiji-executable
            "--headless" "--ij2" "--console" "--run"
            script-name (quote-arg model-folder)))

(defn print-and-log
  "Prints a string message and logs it on a file"
  [log-file msg]
  (print msg)
  (spit log-file msg :append true))


(defn run-script-&-info
  [log-file script-name msg model-folder idx total ]
  (let [m1 (format "- MODEL %d/%d\n" idx total)
        _ (mapv (partial print-and-log log-file) [m1 msg])
        output-map (run-command script-name model-folder)]
    (print-and-log log-file (:out output-map))))

; todo total time taken
(defn -main
  "Loops over models to test and the 2 scripts (inference and comparison)
  Prints info on screen and appends to log"
  ([] (-main LOG-FILE))
  ([log-path]
   (let [log-file (fs/file log-path)
         m-start (format "STARTED TESTING THE %d MODELS ON FIJI\n" (count model-folders))
         m-final (format "FINISHED TESTING THE %d MODELS ON FIJI\nLogs are in: %s"
                         (count model-folders) (str (fs/absolutize log-file)))]
     (spit log-file "")
     (print-and-log log-file m-start)
     ;todo loop over models
     (run-script-&-info log-file (first script-names) (first script-prints) (first model-folders) 1 2)
     (run-script-&-info log-file (second script-names) (second script-prints) (first model-folders) 1 2)
     (print-and-log log-file m-final)
     (System/exit 0))))


(ns reproduce.run-fiji-scripts
  "Run the 2 scripts: inference with DeepImageJ and comparison with Fiji.
  Use bb instead of bash for windows compatibility"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [babashka.fs :as fs]
            [babashka.process :as pr]))

(def COMM-FILE (fs/file ".." "resources" "models_to_test.txt"))
(def FIJI-HOME (fs/file (System/getProperty "user.home") "blank_fiji" "Fiji.App"))
(def fiji-executable (str (first (fs/glob FIJI-HOME "ImageJ*"))))
(def flags ["--headless" "--ij2" "--console" "--run"])
(def arg-name "folder")

(def script-names "Absolute paths to the scripts"
  (->> ["test_1_with_deepimagej.clj" "create_output_metrics.py"]
       (map #(fs/file "src" "reproduce" %))
       (map #(str (fs/absolutize %)))))

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

(defn build-command
  "Builds the complete command string for a given script"
  [script-name model-folder]
  (str fiji-executable " " (str/join " " flags) " "
       script-name " " (quote-arg model-folder)))

; TODO  print info: model number
; todo total time taken
(defn -main
  "Loops over models to test and the 2 scripts (inference and comparison)"
  []
  (pr/shell (build-command (first script-names) (first model-folders)))
  (pr/shell (build-command (second script-names) (first model-folders))))
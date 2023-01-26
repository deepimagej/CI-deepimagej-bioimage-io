(ns reproduce.run-fiji-scripts
  "Run the 2 scripts (inference with DeepImageJ and comparison with Fiji.
  Use bb instead of bash for windows compatibility"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [babashka.process :as pr]))

(def COMM-FILE (fs/file ".." "resources" "models_to_test.txt"))
(def FIJI-HOME (fs/file (System/getProperty "user.home") "blank_fiji" "Fiji.App"))
(def command (str (first (fs/glob FIJI-HOME "ImageJ*"))))
(def flags ["--headless" "--ij2" "--console" " --run"])

; TODO abs path to scripts
(def script-names ["test_1_with_deepimagej.clj" "create_output_metrics.py"])

(defn read-lines
  "Reads every line on a file, returns a vector of strings"
  [file]
  (with-open [rdr (io/reader file)]
    (into [] (line-seq rdr))))

(def model-folders (read-lines COMM-FILE))

; TODO correct quoting of arguments

; TODO string command (to copy paste in terminal)

; TODO  print info: model number

(defn -main []
  (pr/shell (str command " " (str/join " " flags)) (first script-names) (first model-folders))
  )
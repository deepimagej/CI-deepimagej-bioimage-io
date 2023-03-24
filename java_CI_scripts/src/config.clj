(ns config
  "All the configuration constants in one place"
  (:require [clojure [string :as str] [pprint :as ppr]]
            [babashka.fs :as fs]))

(def ROOTS "Paths to folder roots"
  {:collection-root     (fs/absolutize (fs/path ".." "bioimageio-gh-pages" "rdfs"))
   :summa-root          (fs/absolutize (fs/path ".." "test_summaries"))
   :models-root         (fs/absolutize (fs/path ".." "models"))
   :samples-root        (fs/absolutize (fs/path ".." "numpy-tiff-deepimagej"))
   :resources-root      (fs/absolutize (fs/path ".." "resources"))
   :pending-matrix-root (fs/absolutize (fs/path "pending_matrix"))})

(def FILES "Configuration constants that are files"
  {:config        (fs/file (:resources-root ROOTS) "config.edn")
   :logs          {:out (fs/file (:summa-root ROOTS) "fiji_log_out.txt")
                   :err (fs/file (:summa-root ROOTS) "fiji_log_err.txt")}
   :report        (fs/file (:summa-root ROOTS) "report.md")
   :bash-script   (fs/file (:resources-root ROOTS) "test_the_models.sh")
   :models-listed (fs/file (:resources-root ROOTS) "models_to_test.txt")
   :models-vector (fs/file (:resources-root ROOTS) "models_to_test.edn")
   :rdfs-listed   (fs/file (:resources-root ROOTS) "rdfs_to_test.txt")
   :fiji-home     (fs/file (System/getProperty "user.home") "blank_fiji" )
   :summa-readme  (fs/file (:summa-root ROOTS) "Readme.md")})

(def CONSTANTS "Constants that are not files"
  {:fiji-flags             ["--headless" "--ij2" "--console" "--run"]
   :fiji-executable        (str (fs/file (:fiji-home FILES) "Fiji.app"
                                         (if (str/includes? (System/getProperty "os.name") "Windows")
                                           "ImageJ-win64.exe" "ImageJ-linux64")))
   :fiji-download-url      "https://downloads.imagej.net/fiji/archive/20221201-1017/"
   :dij2-download-url      "https://github.com/deepimagej/deepimagej-plugin/releases/download/2.1.15/DeepImageJ_-2.1.15.jar"
   :dij2-deps-url          "https://github.com/deepimagej/deepimagej-plugin/releases/download/2.1.15/dependencies_2115.zip"
   :fiji-scripts-arg-name  "folder"
   :output-metrics-name    "output_metrics.edn"
   :summary-name           "test_summary.yaml"
   :dij-args-filename      "dij_args.edn"
   :mse-threshold          2.5
   :special-headless-chars #{" " "_"}
   :summa-readme-header    "# Report summary"
   :sample-input-name      "sample_input_0.tif"
   :sample-output-name     "sample_output_0.tif"
   :CI-output-name         "CI_OUTPUT.tif"
   :model-dir-name         "the_model"})

(defn absolutize-nested
  "absolutize values of dictionary that are files"
  [dict]
  (into {} (map (fn [[k v]] (vector k (if (map? v) (absolutize-nested v)
                                                   (str (fs/absolutize v))))) dict)))

(defn serialize-config
  " Serialize config into an edn file. Make the files into their absolute path strings"
  ([] (serialize-config (:config FILES)))
  ([config-file]
   (let [full-dict (merge CONSTANTS (absolutize-nested ROOTS) (absolutize-nested FILES))]
     (spit config-file (with-out-str (ppr/pprint full-dict)))
     (printf "Config file generated in %s\n" (str (fs/absolutize config-file))))))
(ns config
  "All the configuration constants in one place"
  (:require [clojure [string :as str] [pprint :as ppr] [edn :as edn]]
            [babashka.fs :as fs]))

; todo fill config constants (start from the end: run-fiji-scripts and test-1)
(def FILES "Configuration constants that are files"
  {:config          (fs/file ".." "resources" "config.edn")
   :collection-root (fs/file ".." "bioimageio-gh-pages" "rdfs")
   :models-root     (fs/file ".." "models")
   :logs            {:out (fs/file ".." "test_summaries" "fiji_log_out.txt")
                     :err (fs/file ".." "test_summaries" "fiji_log_err.txt")}
   :report          (fs/file ".." "test_summaries" "report.md")
   :bash-script     (fs/file ".." "resources" "models_to_test.sh")
   :models-listed   (fs/file ".." "resources" "models_to_test.txt")
   :fiji-home       (fs/file (System/getProperty "user.home") "blank_fiji" "Fiji.app")
   })

(def CONSTANTS "Constants that are not files"
  {:fiji-flags            ["--headless" "--ij2" "--console" "--run"]
   :fiji-executable       (str (fs/file (:fiji-home FILES)
                                        (if (str/includes? (System/getProperty "os.name") "Windows")
                                          "ImageJ-win64.exe" "ImageJ-linux64")))
   :fiji-scripts-arg-name "folder"})

(defn absolutize-nested
  "absolutize values of dictionary that are files"
  [dict]
  (into {} (map (fn [[k v]] (vector k (if (map? v) (absolutize-nested v)
                                                   (str (fs/absolutize v))))) dict)))

(defn serialize-config
  " Serialize config into an edn file
  make the files into their strings"
  ([] (serialize-config (:config FILES)))
  ([config-file]
   (let [full-dict (into CONSTANTS (absolutize-nested FILES))]
     (spit config-file (with-out-str (ppr/pprint full-dict)))
     (printf "Config file generated in %s\n" (str (fs/absolutize config-file))))))
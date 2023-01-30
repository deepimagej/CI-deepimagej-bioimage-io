(ns config
  "All the configuration constants in one place"
  (:require [clojure [string :as str] [pprint :as ppr] [edn :as edn]]
            [babashka.fs :as fs]))

; todo fill config constants (start from the end: run-fiji-scripts and test-1)
(def FILES "Configuration constants that are files"
  {:config (fs/file ".." "resources" "config.edn")
   :collection-root (fs/file ".." "bioimageio-gh-pages" "rdfs")
   :logs {:out (fs/file ".." "test_summaries" "fiji_log_out.txt")
               :err (fs/file ".." "test_summaries" "fiji_log_err.txt")}
   :bash-script (fs/file ".." "resources" "models_to_test.sh")
   })

(def CONSTANTS "Constants that are not files"
  {:fiji-flags ["--headless" "--ij2" "--console" "--run"]})

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
(ns reproduce.compare-output
  (:require [clojure [edn :as edn] [test :refer :all]]
            [clojure.java [io :refer [file]]]))
;this is also run in fiji

;communicate the results in a file (edn) to write tests summaries
; saved in model-folder (in the comm-file)
; output does not exists => dij run failed
; output exists
; - it is not equal to expected output => failed
; - it is equal to expected output => pass

(def comm-file (file (System/getProperty "user.home") "models_to_test.edn"))
(def all-models "All Models in a vector, from the comm-file" (edn/read-string (slurp comm-file)))
(def fiji-home (file (System/getProperty "user.home") "blank_fiji" ))
(def model-dir "the_model")
(def output-name "CI_OUTPUT.tif")

(defn output-created?
  "Predicate says if the output was created from the deepimagej run in specified folder"
  [folder]
  (->> (file folder)
       ; file-seq ; is recursive (!! aka slow for $HOME !!)
       (.listFiles)
       (map #(.getName %))
       (filter #(= % output-name))
       (empty?)
       not))

;; --
;; -- Tests in the same file, as I couldn't find a way to require own ns's in a fiji script
;; --

(deftest output-created?-test
         (is (not (output-created? (.getParent comm-file))))
         (is (output-created? (:model-folder (first all-models)))))

(println (run-tests))
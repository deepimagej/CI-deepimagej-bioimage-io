; command line argument for fiji scripts needs this
; source 1: https://imagej.net/scripting/headless
; source 2: https://imagej.net/scripting/parameters
#@ String (label="", value="something wrong in args") folder ; can be accessed by var user/folder

(ns reproduce.test-1-with-deepimagej
  "Requires the information of the models to be in their model folder in the file dij_args.edn "
  (:require [clojure [edn :as edn] [test :refer :all] [pprint :as ppr]]
            [clojure.java [io :refer [file make-parents copy delete-file]]])
  (:import [ij IJ])
  )

(println "   The folder is:" user/folder)                      ; debug

(def COMM-FILE (file user/folder "dij_args.edn"))
(def DIJ-MODEL (edn/read-string (slurp COMM-FILE)))
(def FIJI-HOME (file (System/getProperty "user.home") "blank_fiji"))
(def MODEL-DIR-NAME "the_model")
(def OUTPUT-NAME "CI_OUTPUT.tif")

(defn copy-file
  [in-dir out-dir filename]
  (copy (file in-dir filename) (file out-dir filename)))

(defn copy-model-folder
  "Copy each file in the model folder to Fiji.app/models
  If only 1 arg, consider Fiji.app in the system home foler"
  ([fiji-home model-folder]
   (let [in-file (file model-folder MODEL-DIR-NAME)
         out-file (file fiji-home "Fiji.app" "models" MODEL-DIR-NAME)
         ls (seq (.listFiles in-file))]
     (make-parents (file out-file "a_file.txt"))
     (mapv #(copy-file in-file out-file (.getName %)) ls)))
  ([model-folder] (copy-model-folder (System/getProperty "user.home") model-folder)))

(defn delete-model-folder
  "Delete the model from Fiji.app/models (once inference is finished)"
  ([fiji-home]
   (let [m-dir (file fiji-home "Fiji.app" "models" MODEL-DIR-NAME)]
     (doseq [f (reverse (file-seq m-dir))]
       (delete-file f))))
  ([] (delete-model-folder (System/getProperty "user.home"))))

(defn test-one-with-deepimagej
  "Test one model with deepimagej"
  [{:keys [dij-arg model-folder input-img]}]
  (with-open [imp (try (IJ/openImage (str model-folder MODEL-DIR-NAME "/" input-img))
                       (catch Exception e (println "-- Error trying to open sample input image")))]
    (if (not (nil? imp))
      (do (copy-model-folder FIJI-HOME model-folder)
          (try (IJ/run imp "DeepImageJ Run" dij-arg)
               (catch Exception e (println "-- Error during deepimagej run")))
          (try (IJ/saveAs "Tiff" (str model-folder OUTPUT-NAME))
               (catch Exception e (println "-- Error trying to save output image")))
          (delete-model-folder FIJI-HOME)))))

(defmacro my-time
  "Variation on clojure.core/time: https://github.com/clojure/clojure/blob/clojure-1.10.1/src/clj/clojure/core.clj#L3884
  This macro returns a map with the time taken and the return value of the expression.
  Useful when timing side effects, no further composition is not usually needed (but still possible)"
  [expr]
  `(let [start# (java.time.Instant/ofEpochMilli (System/currentTimeMillis))
         ret# ~expr ;; evaluates the argument expression
         end# (java.time.Instant/ofEpochMilli (System/currentTimeMillis))
         duration# (java.time.Duration/between start# end#)]
     (hash-map :duration duration# :iso (str duration#)  :return ret#)))

(defn test-one-with-deepimagej-&-info
  "Wrapper for testing a model, adding time and print information"
  [{:keys [name nickname] :as model}]
  (println (format "-- Started testing model: %s" nickname))
  (println (format "   Name: %s" name))
  (println (format "   Time taken: %s" (:iso (my-time (test-one-with-deepimagej model)))))
  (println (format "-- Finished testing model: %s" nickname)))

(defn -main [& args]
  (println "--")
  (test-one-with-deepimagej-&-info DIJ-MODEL))

(-main)
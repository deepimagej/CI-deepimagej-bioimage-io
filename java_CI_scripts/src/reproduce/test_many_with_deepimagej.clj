(ns reproduce.test-many-with-deepimagej
  "Requires that the information for all models is in ~/models_to_test.edn"
  (:require [clojure [edn :as edn] [test :refer :all] [pprint :as ppr]]
            [clojure.java [io :refer [file make-parents copy delete-file]]])
  (:import [ij IJ])
  )

(def COMM-FILE (file (System/getProperty "user.home") "models_to_test.edn"))
(def ALL-MODELS "All Models in a vector, from the comm-file" (edn/read-string (slurp COMM-FILE)))
(def FIJI-HOME (file (System/getProperty "user.home") "blank_fiji"))
(def MODEL-DIR-NAME "the_model")
(def OUTPUT-NAME "CI_OUTPUT.tif")

(defn copy-file
  [in-dir out-dir filename]
  (copy (file in-dir filename) (file out-dir filename)))

(defn copy-model-folder
  "Copy each file in the model folder to Fiji.app/models"
  ([fiji-home model-folder]
   (let [in-file (file model-folder MODEL-DIR-NAME)
         out-file (file fiji-home "Fiji.app" "models" MODEL-DIR-NAME)
         ls (seq (.listFiles in-file))]
     (make-parents (file out-file "a_file.txt"))
     (mapv #(copy-file in-file out-file (.getName %)) ls)))
  ([dij-model] (copy-model-folder (System/getProperty "user.home") dij-model)))

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
  [{:keys [name nickname] :as model} idx total]
  (let [fmt-str " %d/%d (%s)"
        fmt-args [idx total (str nickname)]]
    (println (apply format (concat [(str "-- Testing model" fmt-str)] fmt-args)))
    (println (format "   Name: %s" name))
    (println (format "   Time taken: %s" (:iso (my-time (test-one-with-deepimagej model)))))
    (println (apply format (concat [(str "   Finished testing model" fmt-str)] fmt-args)))))

(defn -main [& args]
  (println "--")
  (println "Hi from main, the args are:" args)
  (println (format "The clojure version is: %s\n" *clojure-version*))
  ;(test-one-with-deepimagej-&-info (first all-models) 1 2)
  (doall (map-indexed (fn [i v]
                        (test-one-with-deepimagej-&-info v (inc i) (count ALL-MODELS)))
                      ALL-MODELS))
  (println "-- Finished execution of testing_many_with_deepimagej."))
;(map-indexed)

;; --
;; -- Tests in the same file, as I couldn't find a way to require own ns's in a fiji script
;; --

(deftest all-models-test
  (is (= (type ALL-MODELS) clojure.lang.PersistentVector))
  (do
    (print (format "There are %d models in %s.\n The first one is:" (count ALL-MODELS) COMM-FILE))
    (ppr/pprint (first ALL-MODELS))))

(println (run-tests))


(def total-time (:iso (my-time (-main "a" 1 "2"))))
(println (format "   Total time taken: %s" total-time))
; make total time taken the return value of script
total-time
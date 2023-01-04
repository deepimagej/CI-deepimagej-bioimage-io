(ns reproduce.test-many-with-deepimagej
  (:require [clojure [edn :as edn] [test :refer :all] [pprint :as ppr]]
            [clojure.java [io :refer [file make-parents copy delete-file]]])
  (:import [ij IJ])
  )

(def comm-file (file (System/getProperty "user.home") "models_to_test.edn"))
(def all-models "All Models in a vector, from the comm-file" (edn/read-string (slurp comm-file)))
(def fiji-home (file (System/getProperty "user.home") "blank_fiji" ))
(def model-dir "the_model")
(def output-name "CI_OUTPUT.tif")

(defn copy-file
  [in-dir out-dir filename]
  (copy (file in-dir filename) (file out-dir filename)))

(defn copy-model-folder
  "Copy each file in the model folder to Fiji.app/models"
  ([fiji-home model-folder]
   (let [in-file (file model-folder model-dir)
         out-file (file fiji-home "Fiji.app" "models" model-dir )
         ls (seq (.listFiles in-file))]
     (make-parents (file out-file "a_file.txt"))
     (mapv #(copy-file in-file out-file (.getName %)) ls)))
  ([dij-model] (copy-model-folder (System/getProperty "user.home") dij-model)))

(defn delete-model-folder
  "Delete the model from Fiji.app/models (once inference is finished)"
  ([fiji-home]
   (let [m-dir (file fiji-home "Fiji.app" "models" model-dir)]
     (doseq [f (reverse (file-seq m-dir))]
       (delete-file f))))
  ([] (delete-model-folder (System/getProperty "user.home"))))

(defn test-one-with-deepimagej
  "Test one model with deepimagej"
  [{:keys [dij-arg model-folder input-img]}]
  (let [imp (IJ/openImage (str model-folder model-dir "/" input-img))]
    (copy-model-folder fiji-home model-folder)
    (try (IJ/run imp "DeepImageJ Run" dij-arg)
         (catch Exception e (println "-- Error during deepimagej run")))
    (try (IJ/saveAs "Tiff" (str model-folder output-name))
         (catch Exception e (println "-- Error trying to save output image")))
    (delete-model-folder fiji-home)))

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
                        (test-one-with-deepimagej-&-info v (inc i) (count all-models)))
                      all-models))
  (println "-- Finished execution of testing_many_with_deepimagej."))
;(map-indexed)

;; --
;; -- Tests in the same file, as I couldn't find a way to require own ns's in a fiji script
;; --

(deftest all-models-test
  (is (= (type all-models) clojure.lang.PersistentVector))
  (do
    (print (format "There are %d models in %s.\n The first one is:" (count all-models) comm-file))
    (ppr/pprint (first all-models))))

(println (run-tests))


(def total-time (:iso (my-time (-main "a" 1 "2"))))
(println (format "   Total time taken: %s" total-time))
; make total time taken the return value of script
total-time
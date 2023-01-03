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
     (do (make-parents (file out-file "a_file.txt"))
         (mapv #(copy-file in-file out-file (.getName %)) ls))))
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
    (IJ/saveAs "Tiff" (str model-folder output-name))
    (delete-model-folder fiji-home)))

(defn test-one-with-info
  "Wrapper for testing a model, adding time and print information"
  [{:keys [name nickname]}])

(defn -main [& args]
  (println "Hi from main, the args are:" args)
  (println "The clojure version is: " *clojure-version*)
  (test-one-with-deepimagej (first all-models)))


;; --
;; -- Tests in the same file, as I couldn't find a way to require own ns's in a fiji script
;; --

(deftest all-models-test
  (is (= (type all-models) clojure.lang.PersistentVector))
  (do
    (print (format "There are %d models in %s.\n The first one is:" (count all-models) comm-file))
    (ppr/pprint (first all-models))))

(println (run-tests))


(-main "a" 1 "2")
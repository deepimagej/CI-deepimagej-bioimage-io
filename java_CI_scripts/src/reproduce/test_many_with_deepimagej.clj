(ns reproduce.test-many-with-deepimagej
  (:require [clojure [edn :as edn] [test :refer :all] [pprint :as ppr]]
            [clojure.java [io :refer [file]]])
  ;(:import [ij IJ])
  )

(def comm-file (file (System/getProperty "user.home") "models_to_test.edn"))
(def all-models "All Models in a vector, from the comm-file"
  (edn/read-string (slurp comm-file)))

(def output-name "CI_OUTPUT.tif")

(defn output-created?
  "Predicate says if the output was created from the deepimagej run in specified folder"
  [folder]
  (->> (file folder)
       ; file-seq ; is recursive (!! aka slow)
       (.listFiles)
       (map #(.getName %))
       (filter #(= % output-name))
       (empty?)
       not))

(defn test-one-with-deepimagej
  "Wrapper for testing a model, adding time and print information"
  [{:keys [name nickname]}])

(defn test-one
  "Test one model with deepimagej"
  [{:keys [dij-arg model-folder input-img]}]
  (let [imp nil ]))

(defn -main [& args]
  (println "Hi from main, the args are:" args)
  (println "The clojure version is: " *clojure-version*))

(-main "a" 1 2)

;; --
;; -- Tests in the same file, as I couldn't find a way to require own ns's in a fiji script
;; --

(deftest all-models-test
  (is (= (type all-models) clojure.lang.PersistentVector))
  (do
    (print (format "There are %d models in %s.\n The first one is:" (count all-models) comm-file))
    (ppr/pprint (first all-models))))

(deftest output-created?-test
  (is (not (output-created? (.getParent comm-file))))
  (is (output-created? (:model-folder (first all-models)))))

(println (run-tests))

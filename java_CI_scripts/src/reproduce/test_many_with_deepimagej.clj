(ns reproduce.test-many-with-deepimagej
  (:require [clojure [edn :as edn] [test :refer :all] [pprint :as ppr]])
  ;(:import [ij IJ])
  )

(def comm-file (clojure.java.io/file (System/getProperty "user.home") "models_to_test.edn"))
(def all-models "All Models in a vector, from the comm-file"
  (edn/read-string (slurp comm-file)))

(defn test-one-with-deepimagej
  [model])

(defn -main [& args]
  (println "Hi from main, the args are:" args)
  (println "The clojure version is: " *clojure-version*))

(-main "a" 1 2)

;; -- Tests in the same file, as I couldn't file a way to require own ns's in a fiji script

(deftest all-models-test
  (do
    (print (format "There are %d models in %s.\n The first one is:" (count all-models) comm-file))
    (ppr/pprint (first all-models))))

(println (run-tests))

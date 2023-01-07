(ns downloads-test
  (:require [downloads :refer :all]
            [models :refer [build-model]]
            [collection :refer [file-json->vector get-rdfs-to-test]]
            [test-setup :refer [rdf-paths load-test-paths]]
            [clojure.test :refer :all]))

(def model-records (atom nil))
(def all-model-records (atom nil))

(defn load-model-records
  [test-fn]
  (let [all-rdfs (file-json->vector "pending_matrix/all_models.json")]
    (reset! model-records (map (fn [[k v]] (build-model @v)) rdf-paths))
    (reset! all-model-records (map  #(build-model %) (get-rdfs-to-test all-rdfs))))
  (test-fn))





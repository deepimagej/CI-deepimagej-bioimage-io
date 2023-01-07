(ns test-setup
  (:require [collection :refer [COLLECTION-ROOT file-json->vector get-rdfs-to-test]]
            [models :refer [build-model]]
            [clojure [test :refer :all] [edn :as edn]]
            [babashka.fs :as fs]))

;Initial setup, only needs the COLLECTION-ROOT

(def rdf-paths "Paths to different rdfs for testing"
  {:an-rdf (atom nil) :tf-rdf (atom nil) :pt-rdf (atom nil)})

(defn load-test-paths
  "Loads paths of rdfs of different models for testing:
  - A model without dij config (with state dict)
  - A tensorflow model (with keras) with pre- and post- process
  - A torchscript model (with state dict) with pre-process"
  [test-fn]
  (reset! (:an-rdf rdf-paths)
          (fs/path COLLECTION-ROOT "10.5281" "zenodo.6334881" "6346477" "rdf.yaml"))
  (reset! (:tf-rdf rdf-paths)
          (fs/path COLLECTION-ROOT "10.5281" "zenodo.5749843" "5888237" "rdf.yaml"))
  (reset! (:pt-rdf rdf-paths)
          (fs/path COLLECTION-ROOT "10.5281" "zenodo.5874741" "5874742" "rdf.yaml" ))
  (test-fn))

;Setup for 'downloads/*.clj and after, assumes 'collection and 'models already tested and working

(def model-records (atom nil))
(def all-model-records (atom nil))

(defn load-model-records
  [test-fn]
  (let [all-rdfs (file-json->vector "pending_matrix/all_models.json")]
    (reset! model-records (map (fn [[k v]] (build-model @v)) rdf-paths))
    (reset! all-model-records (map #(build-model %) (get-rdfs-to-test all-rdfs))))
  (test-fn))

(def an-edn (edn/read-string (slurp (fs/file "test" "resources" "an.edn"))))
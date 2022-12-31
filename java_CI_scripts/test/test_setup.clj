(ns test-setup
  (:require [collection :refer [COLLECTION-ROOT]]
            [clojure.test :refer :all]
            [babashka.fs :as fs]))


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
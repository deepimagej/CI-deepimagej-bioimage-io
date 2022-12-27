(ns test-setup
  (:require [clojure.test :refer :all]
            [collection :refer [COLLECTION-ROOT]]
            [babashka.fs :as fs]))


(def rdf-paths "Paths to different rdfs for testing"
  {:an-rdf (atom nil) :tf-rdf (atom nil) :pt-rdf (atom nil)})

(defn load-test-paths
  [test-fn]
  (reset! (:an-rdf rdf-paths)
          (fs/path COLLECTION-ROOT "10.5281" "zenodo.6334881" "6346477" "rdf.yaml"))
  (reset! (:tf-rdf rdf-paths)
          (fs/path COLLECTION-ROOT "10.5281" "zenodo.5749843" "5888237" "rdf.yaml"))
  (reset! (:pt-rdf rdf-paths)
          (fs/path COLLECTION-ROOT "10.5281" "zenodo.5874741" "5874742" "rdf.yaml" ))
  (test-fn))
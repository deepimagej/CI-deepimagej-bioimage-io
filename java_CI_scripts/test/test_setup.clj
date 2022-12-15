(ns test-setup
  (:require [clojure.test :refer :all]
            [collection :refer [COLLECTION-ROOT]]
            [babashka.fs :as fs]))

(def test-rdf-path (atom nil))

(defn load-test-data
  [test-fn]
  (reset! test-rdf-path
          (fs/path (str COLLECTION-ROOT) "10.5281" "zenodo.6334881" "6346477" "rdf.yaml"))
  (test-fn))
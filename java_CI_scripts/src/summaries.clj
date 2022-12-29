(ns summaries
  (:require [collection :refer [COLLECTION-ROOT]]
            [clj-yaml.core :as yaml]
            [babashka.fs :as fs]))

(def SUMMA-ROOT "path to the test summaries" (fs/path ".." "test_summaries"))

(defn get-parent-components
  "Gets the (seq) of components of parent directory structure (from root) of a given rdf path"
  ([root path] (butlast (fs/components (fs/relativize root path))))
  ([rdf-path] (get-parent-components COLLECTION-ROOT rdf-path)))

(defn new-root-path
  "Returns a path with a new root"
  [old-root new-root path]
  (apply fs/path (conj (get-parent-components old-root path) new-root)))

(defn gen-summa-path
  "Gets the path corresponding to the test summary of an rdf-path"
  ([coll-root summa-root rdf-path] (new-root-path coll-root summa-root rdf-path))
  ([rdf-path] (gen-summa-path COLLECTION-ROOT SUMMA-ROOT rdf-path)))

(defn create-summa-dir
  "Creates directory for resulting test summaries given the path to an rdf"
  ([coll-root summa-root rdf-path]
   (fs/create-dirs (gen-summa-path coll-root summa-root rdf-path)))
  ([rdf-path]
   (create-summa-dir COLLECTION-ROOT SUMMA-ROOT rdf-path)))

;TODO
; decide api
; generate dictionary
; write yaml
(defn gen-summa-dict
  [])

(defn write-test-summary
  "status is a literal string: passed or failed"
  [model status & kwargs])

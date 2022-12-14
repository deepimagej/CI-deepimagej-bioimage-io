(ns summaries
  (:require [babashka.fs :as fs]
            [collection :refer [COLLECTION-ROOT]]))

;generate hash-maps -> to write yaml
; use (format)?

(def SUMMA-ROOT "path to the test summaries" (fs/path ".." "test_summaries"))

(defn get-parent-components
  "Gets the (seq) of components of parent directory structure (from root) of a given rdf path"
  ([coll-root rdf-path] (butlast (fs/components (fs/relativize coll-root rdf-path))))
  ([rdf-path] (get-parent-components COLLECTION-ROOT rdf-path)))

(defn get-summa-path
  "Gets the path corresponding to the test summary of an rdf-path"
  ([summa-root rdf-path] (apply fs/path (conj (get-parent-components rdf-path) summa-root)))
  ([rdf-path] (get-summa-path SUMMA-ROOT rdf-path)))

(defn create-summa-dir
  "Creates directory for resulting test summaries given the path to an rdf"
  [rdf-path]
  (fs/create-dirs (get-summa-path rdf-path)))
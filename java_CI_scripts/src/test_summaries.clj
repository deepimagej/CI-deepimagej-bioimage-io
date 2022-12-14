(ns test-summaries
  (:require [babashka.fs :as fs]
            [collection :refer [COLLECTION-ROOT]]))

;generate hash-maps -> to write yaml
; use (format)?

(def TEST-SUM-ROOT "path to the test summaries" (fs/path ".." "test_summaries"))

(defn get-parent-dirs
  "Gets the components of parent directory structure (from root) of a given rdf path"
  ([coll-root rdf-path] (butlast (fs/components (fs/relativize coll-root rdf-path))))
  ([rdf-path] (get-parent-dirs COLLECTION-ROOT rdf-path)))

(defn get-test-sum-path
  "Gets the path corresponding to the test summary of an rdf-path"
  ([test-s-root rdf-path] (apply fs/path (conj (get-parent-dirs rdf-path) test-s-root)))
  ([rdf-path] (get-test-sum-path TEST-SUM-ROOT rdf-path)))

(defn create-test-sum-dir
  "Creates directory for resulting test summaries given the path to an rdf"
  [test-root rdf-path]
  (let [test-path (apply fs/path (conj ))]
    (fs/create-dirs test-path))
  )

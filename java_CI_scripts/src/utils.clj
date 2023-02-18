(ns utils
  "Generic functions"
  (:require [config :refer [ROOTS]]
            [clojure.java.io :as io]
            [babashka.fs :as fs]))

(defn count-dict
  "Counts dictionary entries which are seqs"
  [d] (reduce (fn [m [k v]] (assoc m k (count v))) {}  d))

(defn read-lines
  "Reads every line on a file, returns a vector of strings"
  [file]
  (with-open [rdr (io/reader file)]
    (into [] (line-seq rdr))))

(defn local-time
  "Returns local date and time as string in ISO Format"
  ([] (local-time "Europe/Paris"))
  ([zone-str]
   (str (java.time.LocalDateTime/now (java.time.ZoneId/of zone-str)))))

(defn select-key->vec
  "Select a key from a dictionary, but return a vector of 2 elements.
  This is what happens to iterating over a map and destructuring"
  [dict key-word]
  (first (select-keys dict [key-word])))

; TODO: use this functions instead of the ones in s.summary
(defn get-parent-components
  "Gets the (seq) of components of parent directory structure (from root) of a given rdf path"
  ([root path] (butlast (fs/components (fs/relativize root path))))
  ([rdf-path] (get-parent-components (:collection-root ROOTS) rdf-path)))

(defn new-root-path
  "Returns a path with a new root"
  [old-root new-root path]
  (apply fs/path (conj (get-parent-components old-root path) new-root)))
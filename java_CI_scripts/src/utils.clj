(ns utils
  "Generic functions"
  (:require [config :refer [ROOTS FILES CONSTANTS]]
            [clojure.string :as str]
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

(defn get-parent-components
  "Gets the (seq) of components of parent directory structure (from root) of a given rdf path"
  ([root path] (butlast (fs/components (fs/relativize root path))))
  ([rdf-path] (get-parent-components (:collection-root ROOTS) rdf-path)))

(defn new-root-path
  "Returns a path with a new root"
  [old-root new-root path]
  (apply fs/path (conj (get-parent-components old-root path) new-root)))

(comment "Improved implementation, but breaks calls"
         (defn new-root-path
           "Returns a path with a new root"
           [old-root new-root path]
           (apply fs/path (conj (fs/components (fs/relativize old-root path)) new-root))) )

(defn print-and-log
  "Prints a string message and logs it on all log files provided"
  ([msg] (apply print-and-log msg (vals (:logs FILES))))
  ([msg & log-files]
   (print msg)
   (flush)
   (mapv #(spit % msg :append true) log-files)))

(defn before-str-content
  "Returns all the contents of a string, before the pattern"
  [s s-pattern]
  (str/split s (re-pattern s-pattern)))

(defn original-file-content
  "Returns the contents of a file, only keeping contents up to a string pattern"
  [file pattern]
  (let [content (if (fs/exists? file) (slurp file) "")
        content-to-keep (first (utils/before-str-content content pattern))]
    content-to-keep))

(defmacro my-time
  "Variation on clojure.core/time: https://github.com/clojure/clojure/blob/clojure-1.10.1/src/clj/clojure/core.clj#L3884
  This macro returns a map with the time taken (duration) and the return value of the expression.
  Useful when timing side effects, when further composition is not usually needed (but still possible)"
  [expr]
  `(let [start# (java.time.Instant/ofEpochMilli (System/currentTimeMillis))
         ret# ~expr ;; evaluates the argument expression
         end# (java.time.Instant/ofEpochMilli (System/currentTimeMillis))
         duration# (java.time.Duration/between start# end#)]
     (hash-map :duration duration# :iso (str duration#)  :return ret#)))
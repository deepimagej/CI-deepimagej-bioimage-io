(ns utils
  "Generic functions"
  (:require [clojure.java.io :as io]))

(defn count-dict
  "Counts dictionary entries which are seqs"
  [d] (reduce (fn [m [k v]] (assoc m k (count v))) {}  d))

(defn read-lines
  "Reads every line on a file, returns a vector of strings"
  [file]
  (with-open [rdr (io/reader file)]
    (into [] (line-seq rdr))))

(defn local-time
  "Returns local date and time"
  ([] (local-time "Europe/Paris"))
  ([zone-str]
   (str (java.time.LocalDateTime/now (java.time.ZoneId/of zone-str)))))
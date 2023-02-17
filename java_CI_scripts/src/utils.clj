(ns utils
  "Generic functions")

(defn count-dict
  "Counts dictionary entries which are seqs"
  [d] (reduce (fn [m [k v]] (assoc m k (count v))) {}  d))
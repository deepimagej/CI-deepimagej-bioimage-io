#! /usr/bin/env bb

(def files (file-seq (fs/file "src")))

(def clj-files (filter #(= "clj" (fs/extension %)) files))

(defn n-lines [file] (count (clojure.string/split-lines (slurp file))))

(def total-loc (reduce + (map n-lines clj-files)))

(mapv #(println (fs/file-name %) ": " (n-lines %)) clj-files)
(printf "Total LOC: %d, average %.1f\n" total-loc (/ total-loc (float (count clj-files))))

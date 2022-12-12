(ns collection
  (:require [babashka.fs :as fs]))

;(slurp "abc")

(defn -main [& args]
  (println *command-line-args*)
  (println args)
  args)
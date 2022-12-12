(ns collection
  (:require [babashka.fs :as fs]
            [cheshire.core :as json]))

(comment
  (json/parse-string (slurp (fs/file "pending_matrix" "two_models.json")) true)
  )
;(slurp "abc")
(defn json->vector
  "Returns the parsed list of resources/versions to test given a string json"
  [str-json]
  (let [parsed (json/parse-string str-json true)
        {the-list :include} parsed]
    the-list))

(defn -main
  "-s input is a raw json string, -j input is a file.json"
  [& args]
  (println *command-line-args*)
  (println args)
  args)
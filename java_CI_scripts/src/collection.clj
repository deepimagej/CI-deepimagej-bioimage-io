(ns collection
  (:require [babashka.fs :as fs]
            [cheshire.core :as json]))

(def COLLECTION-ROOT (fs/path ".." "bioimageio-gh-pages" "rdfs"))
(defn str-json->vector
  "Returns the parsed list of resources/versions to test given a raw json string"
  [str-json]
  (let [parsed (json/parse-string str-json true)
        {the-list :include} parsed]
    the-list))
(defn file-json->vector
  "Returns the parsed list of resources/versions to test given a json file"
  [file-json]
  (str-json->vector (slurp file-json)))

(defn filter-rdfs
  "Keeps only the rdf.yaml from a seq of paths"
  [paths]
  (filter #(= (fs/file-name %) "rdf.yaml") paths))

(defn resource->paths
  "Takes a resource/version to test and returns its path(s) (multiple if globbing)"
  [root resource-map]
  (let [{:keys [resource_id version_id]} resource-map]
    (cond
      (= resource_id "**") (filter-rdfs (fs/glob root "**"))
      (= version_id "**") (filter-rdfs (fs/glob (fs/path root resource_id) "**"))
      :else (seq (fs/path root resource_id version_id "rdf.yaml")))))



(defn -main
  "-s input is a raw json string, -j input is path to a file.json
  Invoke with bb -m collection <args> in the java_CI_scripts working directory"
  [& args]
  (println *command-line-args*)
  (println args)
  args)
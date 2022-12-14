(ns collection
  (:require [babashka.fs :as fs]
            [cheshire.core :as json]))

(def COLLECTION-ROOT "Path of the cloned repository" (fs/path ".." "bioimageio-gh-pages" "rdfs"))

(defn str-json->vector
  "Returns the parsed list of resources/versions to test, given a raw json string"
  [str-json]
  (let [parsed (json/parse-string str-json true)]
    (:include parsed)))

(defn file-json->vector
  "Returns the parsed list of resources/versions to test, given a json file"
  [file-json]
  (str-json->vector (slurp file-json)))

(defn filter-rdfs
  "Keeps only the rdf.yaml from a seq of paths"
  [paths]
  (filter #(= (fs/file-name %) "rdf.yaml") paths))

(defn resource->paths
  "Takes a resource/version to test and returns list of rdf path(s) (multiple if globbing).
  If only 1 argument is given, uses COLLECTION-ROOT as root path"
  ([root resource-map]
   (let [{:keys [resource_id version_id]} resource-map]
     (cond
       (= resource_id "**") (filter-rdfs (fs/glob root "**"))
       (= version_id "**") (filter-rdfs (fs/glob (fs/path root resource_id) "**"))
       :else (vector (fs/path root resource_id version_id "rdf.yaml")))))
  ([resource-map] (resource->paths COLLECTION-ROOT resource-map)))

(defn get-rdfs-to-test
  "Compiles a list of rdf paths that need to be tested, given a list of resource/versions maps.
  If only 1 argument is given, uses COLLECTION-ROOT as root path"
  ([root resources-vector] (set (flatten (map #(resource->paths root %) resources-vector))))
  ([resources-vector] (get-rdfs-to-test COLLECTION-ROOT resources-vector)))

(defn write-paths-in-file
  "Writes a list of paths into a file,"
  [paths-list filename])
;TODO
; make newline separated string from paths-list (optional absolutize)
; parse rdfs them -> get all models

(defn -main
  "-s input is a raw json string, -j input is path to a file.json
  Invoke with bb -m collection <args> in the java_CI_scripts working directory"
  [& args]
  (println *command-line-args*)
  (println args)
  args)
;TODO test main
; input -j flag two_models.json
; print absolutized paths of rdfs to test
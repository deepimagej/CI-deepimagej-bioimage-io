(ns reproduce.communicate
  (:require [clojure.string :as str]
            [babashka.fs :as fs]))

(def COMM-ROOT (System/getProperty "user.home"))

(def comm-file
  "File with the data structures of the models to test with DIJ headless"
  (fs/file COMM-ROOT "models_to_test.edn"))

(defrecord DijModel [dij-arg model-folder input-img output-img])

(defn format-axes
  "Changes format of axes from rdf (e.g. byzxc) to the one needed for DIJ (e.g. Y,X,Z,C)"
  [s]
  (->> (str/replace s #"b" "")
        str/upper-case
       (str/join ",")))

(defn format-tiles
  "Use as tile the shape in the dij config (separated by commas, not by x's).
  Remove the first element (it is always the batch??)"
  [s]
  (->> (str/split s #" x ")
      rest
      (str/join ",")))

; todo use (format)
(defn build-dij-arg
  "Builds the argument string needed for the DeepImageJ Run command"
  [model-record])

(defn build-dij-model
  [model-record]
  )


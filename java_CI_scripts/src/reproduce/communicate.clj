(ns reproduce.communicate
  (:require [clojure [string :as str] [pprint :as ppr]]
            [clojure.java [io :refer [as-url]]]
            [babashka.fs :as fs]))

(def COMM-ROOT (System/getProperty "user.home"))

(def comm-file
  "File with the data structures of the models to test with DIJ headless"
  (fs/file COMM-ROOT "models_to_test.edn"))

(defrecord DijArg [model format preprocessing postprocessing axes tile logging])
(defrecord DijModel [nickname name dij-arg model-folder input-img output-img])

(defn format-axes
  "Changes format of axes from rdf (e.g. byzxc) to the one needed for DIJ (e.g. Y,X,Z,C)"
  [s]
  (->> (str/replace s #"b" "")
        str/upper-case
       (str/join ",")))

(defn format-tiles
  "Use as tile the shape in the dij config (separated by commas, not by x's).
  Ignore the first element if it is a 1 (it is always the batch??)"
  [s] (->> (str/split s #" x ")
           (map #(Integer/parseInt %))
           (split-with (partial >= 1))
           last
           (str/join ",")))

(defn weight-format
  "Gets the weight format from a model record"
  [model-record]
  (let [w-list (:weights model-record)
        w-types (filter #(not (nil? (:type %))) w-list)]
    (:type (first w-types))))

(defn get-pprocess
  [k model-record]
  (let [not-found {:pre-p  "no preprocessing"
                   :post-p "no postprocessing"}
        pp-map (first (filter #(= (:type %) k) (:p*process model-record)))
        script (:script pp-map)]
    (if script script (k not-found))))

(defn build-dij-arg
  "Builds the argument string needed for the DeepImageJ Run command
  All args are required and are needed in the right order"
  [model-record]
  (let [input-tensor (first (filter #(= (:type %)) (:tensors model-record)))]
    (map->DijArg {:model          (:name model-record)
                  :format         (weight-format model-record)
                  :preprocessing  (get-pprocess :pre-p model-record)
                  :postprocessing (get-pprocess :post-p model-record)
                  :axes           (format-axes (:axes input-tensor))
                  :tile           (format-tiles (:shape input-tensor))
                  :logging        "Normal"})))

(defn bracketize
  "Surround a string with [brackets] (all dij args need this)"
  [s] (str "[" s "]"))

(defn dij-arg-str
  "Makes the DIJ argument as a string"
  [model-record]
  (let [arg-record (build-dij-arg model-record)
        s-keys #{:model :format :preprocessing :postprocessing}
        surround #(if (contains? s-keys %) bracketize identity)]
    (->> (map (fn [[k v]] (format "%s=%s" (name k) ((surround k) v))) arg-record)
        (str/join " "))))

(defn get-name-from-url
  "gets the file-name of a url string"
  [str-url]
  (-> (as-url str-url) .getPath fs/path fs/file-name))

(defn get-test-images
  [model-record]
  (reduce #(assoc %1 (:type %2) (get-name-from-url (:sample %2))) {} (:tensors model-record)))

(defn get-model-folder
  "Gets the model path as a string and with the file separators needed in an imageJ script"
  [model-record]
  (-> (fs/absolutize (get-in model-record [:paths :model-dir-path]))
      (str fs/file-separator)
      (str/replace #"\\" "/")))

(defn build-dij-model
  [model-record]
  (let [{:keys [inputs outputs]} (get-test-images model-record)]
    (map->DijModel {:nickname     (:nickname model-record)
                    :name         (:name model-record)
                    :dij-arg      (dij-arg-str model-record)
                    :model-folder (get-model-folder model-record)
                    :input-img    inputs
                    :output-img   outputs})))

(defn write-comm-file
  "Writes the edn file. Communication file between this CI and the fiji script to run dij headless"
  ([dij-models file]
   (spit file (with-out-str (ppr/pprint (mapv #(into {} %) dij-models)))))
  ([dij-models] (write-comm-file dij-models comm-file)))

(defn write-rdfs
  "Write a list of paths to the rdfs to test
  Communicate to python script to generate tiff from numpy"
  ([model-records]
   (write-rdfs model-records (fs/file ".." "numpy-tiff-deepimagej" "resources" "rdfs_to_test.txt")))
  ([model-records file]
   (as-> model-records s
         (map #(str (fs/absolutize (get-in % [:paths :rdf-path]))) s)
         (str/join \newline s)
         (str/replace s #"\\" "/")
         (spit file s))))
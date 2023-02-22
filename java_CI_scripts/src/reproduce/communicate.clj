(ns reproduce.communicate
  "Creates files with information for the fiji scripts that will do the testing.
  Assumes model folders are populated"
  (:require [config :refer [ROOTS FILES CONSTANTS]]
            [clojure [string :as str] [pprint :as ppr] [edn :as edn]]
            [clojure.java [io :refer [as-url]]]
            [babashka.fs :as fs]))

(def comm-file
  "File with the data structures of the models to test with DIJ headless"
  (fs/file (:resources-root ROOTS) "models_to_test.edn"))

(defrecord DijArg [model format preprocessing postprocessing axes tile logging])
(defrecord DijModel [nickname name dij-arg model-folder input-img output-img])

(defn format-axes
  "Changes format of axes from rdf (e.g. byzxc) to the one needed for DIJ (e.g. Y,X,Z,C)"
  [s]
  (->> (str/replace s #"b" "")
        str/upper-case
       (str/join ",")))

(defn get-input-shape
  "The tile will be the original shape of the input image (without the initial batch).
  This information comes from the numpy, is stored in the file input_shape.edn"
  [model-record]
  (let [{{path :samples-path} :paths} model-record]
    (edn/read-string (slurp (fs/file path "input_shape.edn")))))

(defn format-tiles-str
  "String: Use as tile the shape in the dij config (separated by commas, not by x's).
  Ignore the first element if it is a 1 (it is always the batch) (but order messed up in the yaml)"
  [s]
  (->> (str/split s #" x ")
       (map #(Integer/parseInt %))
       (split-with (partial >= 1))
       last
       (str/join ",")))

(defn format-tiles
  "Vector: remove the first element (batch)"
  [vec-shape]
  (str/join "," (rest vec-shape)))

(defn weight-format
  "Gets the weight format from a model record"
  [model-record]
  (let [w-list (:weights model-record)
        w-types (filter #(not (nil? (:type %))) w-list)]
    (:type (first w-types))))

(defn get-p*process
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
                  :preprocessing  (get-p*process :pre-p model-record)
                  :postprocessing (get-p*process :post-p model-record)
                  :axes           (format-axes (:axes input-tensor))
                  ;:tile           (format-tiles-str (:shape input-tensor))
                  :tile           (format-tiles (get-input-shape model-record))
                  :logging        "Normal"})))

(defn bracketize
  "Surround a string with [brackets] if it has special characters (spaces, underscores, ...)"
  [s]
  (let [list-contains-special? (map #(str/includes? s %) (:special-headless-chars CONSTANTS))]
    ; idiom for applying OR to a list of booleans
    (if (some identity list-contains-special?)
      (str "[" s "]") s)))

(defn dij-arg-str
  "Makes the DIJ argument as a string"
  [model-record]
  (let [arg-record (build-dij-arg model-record)
        s-keys #{:model :preprocessing :postprocessing}
        surrounded #(if (contains? s-keys %) bracketize identity)]
    (->> (map (fn [[k v]] (format "%s=%s" (name k) ((surrounded k) v))) arg-record)
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
  "Note: the name of the test images will no longer be the one in the yaml,
  because all tiffs generated from numpy have the same name."
  [model-record]
  (let [{:keys [inputs outputs]} (get-test-images model-record)]
    (map->DijModel {:nickname     (:nickname model-record)
                    :name         (:name model-record)
                    :dij-arg      (dij-arg-str model-record)
                    :model-folder (get-model-folder model-record)
                    :input-img    "sample_input_0.tif"
                    :output-img   "sample_output_0.tif"})))

(defn write-dij-model
  "Writes the serialized dij model in the model folder"
  [model-record]
  (let [folder (get-in model-record [:paths :model-dir-path])
        content (with-out-str (ppr/pprint (into {} (build-dij-model model-record))))]
    (spit (fs/file folder (:dij-args-filename CONSTANTS)) content)))

(defn write-comm-file
  "Writes the edn file. Communication file between this CI and the fiji script to run dij headless"
  ([dij-models file]
   (spit file (with-out-str (ppr/pprint (mapv #(into {} %) dij-models)))))
  ([dij-models] (write-comm-file dij-models (:models-vector FILES))))

(defn write-absolute-paths
  "Write a list of paths to test (path type chosen as arg: rdf, model-dir...)
  Communicate to python script to generate tiff from numpy with rdf paths"
  ([model-records path-k]
   (write-absolute-paths model-records path-k (fs/file (:resources-root ROOTS) "absolute_paths.txt")))
  ([model-records path-k file]
   (as-> model-records s
         (map #(str (fs/absolutize (get-in % [:paths path-k]))) s)
         (str/join \newline s)
         (str/replace s #"\\" "/")
         (spit file s))))

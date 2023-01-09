(ns downloads.download
  (:require [clojure.java.io :refer [as-url copy]]
            [babashka [curl :as curl] [fs :as fs]]))

(defmacro my-time
  "Variation on clojure.core/time: https://github.com/clojure/clojure/blob/clojure-1.10.1/src/clj/clojure/core.clj#L3884
  This macro returns a map with the time taken (duration) and the return value of the expression.
  Useful when timing side effects, when further composition is not usually needed (but still possible)"
  [expr]
  `(let [start# (java.time.Instant/ofEpochMilli (System/currentTimeMillis))
         ret# ~expr ;; evaluates the argument expression
         end# (java.time.Instant/ofEpochMilli (System/currentTimeMillis))
         duration# (java.time.Duration/between start# end#)]
     (hash-map :duration duration# :iso (str duration#)  :return ret#)))

(defn get-url-response
  "Gets the response of a url as a hash map"
  [^String url]
  (let [basic-opts {:as :bytes :throw false},
        opts (if (= "Windows 10" (System/getProperty "os.name"))
               (assoc basic-opts :compressed false)
               basic-opts)]
    (curl/get url opts)))

(defn get-url-filename
  "Gets the filename from a url"
  [^String url]
  (->> (as-url url) (.getPath) fs/path fs/file-name))

(defn byte-arr->file!
  "Save a byte-array as a file. Ref source https://gist.github.com/philippkueng/11377226"
  [b-arr dir file-name]
  (copy b-arr (fs/file dir file-name)))

(defn get-weights-to-download
  "Weights with type nil are not compatible with Deepimagej (but may have a valid source url)"
  [model-record]
  (let [valid-weights (filter #(not (nil? (:type %))) (:weights model-record))]
    (map :source valid-weights)))

(defn get-images-to-download
  [model-record]
  (filter #(not (nil? %)) (map :sample (:tensors model-record))))

(defn get-urls-to-download
  "Gets all the urls that need to be downloaded to test a model record (weights and images)"
  [model-record])

(defn populate-model-folder
  "Downloads all files from the model-record.
  Copies the rdf and the p*processing from local folders."
  [model-record])
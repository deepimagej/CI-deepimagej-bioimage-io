(ns downloads.download
  (:require [clojure.java.io :refer [as-url copy]]
            [babashka [curl :as curl] [fs :as fs]]))

(def model-dir-name "the_model")

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
  [dir b-arr file-name]
  (let [dest-file (fs/file dir file-name)]
    (copy b-arr (fs/file dir file-name))
    dest-file))

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
  [model-record]
  (concat (get-images-to-download model-record) (get-weights-to-download model-record)))

(defn get-destination-folder
  ([model-record]
   (get-destination-folder model-record model-dir-name))
  ([model-record model-dir-name]
   (fs/file (get-in model-record [:paths :model-dir-path]) model-dir-name)))

(defn save-file-with-info
  "Given a my-timed curl-get-response, and a file, saves body (byte-arr) in file verbosely
  Returns written bytes (0 means download failed)"
  [folder timed-response file-name]
  (let [result-file (byte-arr->file! folder (:return timed-response) file-name)
        written-bytes (.length result-file)]
    (println (format "Downloaded %s (%d bytes)" result-file written-bytes))
    (println (format "Took: %s" (:iso timed-response)))
    written-bytes))

(defn download-into-model-folder
  ([model-record]
   (download-into-model-folder model-record model-dir-name))
  ([model-record model-dir-name]
   (let [urls (get-urls-to-download model-record)
         file-names (map get-url-filename urls)
         timed-responses (map #(my-time (:body (get-url-response %))) urls)
         folder-file (get-destination-folder model-record model-dir-name)]
     ;(doall (pmap (partial byte-arr->file! folder-file) responses file-names))
     (doall (pmap (partial save-file-with-info folder-file) timed-responses file-names))))
  )

(defn populate-model-folder
  "Downloads all needed urls from the model-record into local files.
  Copies the rdf and the p*processing from local folders."
  ([model-record]
   (populate-model-folder model-record model-dir-name))
  ([model-record model-dir-name]
   (let [folder-file (get-destination-folder model-record model-dir-name)]
     (fs/create-dirs folder-file)

     ;todo copy rdf.yaml
     ;todo copy p*processing scripts
     ))
  )

; (my-time (populate-model-folder (second @model-records)))
; with (doall (map
; => :iso "PT1M24.215S"
; with (doall (pmap
; => :iso "PT54.442S"
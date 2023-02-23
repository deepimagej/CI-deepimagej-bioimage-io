(ns downloads.download
  (:require [config :refer [ROOTS CONSTANTS]]
            utils
            [clojure.java.io :as io]
            [clojure.string :as str]
            [babashka [curl :as curl] [fs :as fs]]))

(def MODEL-DIR "the_model")

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
  "Gets the response of a URL as a hash map"
  [^String url]
  (let [basic-opts {:as :bytes :throw false},
        opts (if (str/includes? (System/getProperty "os.name") "Windows")
               (assoc basic-opts :compressed false)
               basic-opts)]
    (curl/get url opts)))

(defn get-url-filename
  "Gets the filename from a url"
  [^String url]
  (->> (io/as-url url) (.getPath) fs/path fs/file-name))

(defn byte-arr->file!
  "Save a byte-array as a file. Ref source https://gist.github.com/philippkueng/11377226"
  [dir b-arr file-name]
  (let [dest-file (fs/file dir file-name)]
    (io/copy b-arr (fs/file dir file-name))
    dest-file))

(defn get-weights-to-download
  "Weights with type nil are not compatible with Deepimagej (but may have a valid source url)"
  [model-record]
  (let [valid-weights (filter #(not (nil? (:type %))) (:weights model-record))]
    (map :source valid-weights)))

(defn get-images-to-download
  [model-record]
  (filter #(not (nil? %)) (map :sample (:tensors model-record))))

(defn get-attachments-to-download
  "Chooses the preprocessing files to download from the links in the attachments"
  [model-record]
  (filter #(or (str/ends-with? % "ijm") (str/ends-with? % "txt"))
          (get-in model-record [:rdf-info :attach])))

(defn get-urls-to-download
  "Gets all the urls that need to be downloaded to test a model record (weights and images)"
  [model-record]
  (concat  (get-weights-to-download model-record)
           ;(get-images-to-download model-record)
           (get-attachments-to-download model-record)))

(defn get-destination-folder
  "Gets the file corresponding to the model folder for a given rdf"
  ([model-record]
   (get-destination-folder model-record MODEL-DIR))
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
    (flush)
    written-bytes))

(defn download-into-model-folder
  ([model-record]
   (download-into-model-folder model-record MODEL-DIR))
  ([model-record model-dir-name]
   (let [urls (get-urls-to-download model-record)
         file-names (map get-url-filename urls)
         timed-responses (map #(my-time (:body (get-url-response %))) urls)
         folder-file (get-destination-folder model-record model-dir-name)]
     ;(doall (pmap (partial byte-arr->file! folder-file) responses file-names))
     (doall (pmap (partial save-file-with-info folder-file) timed-responses file-names)))))

(defn get-correct-sample-images
  "Get the sample images for the model folder, choose the manual sample tiffs if they exist"
  [model-record]
  (let [sample-path (get-in model-record [:paths :samples-path])
        sample-input (fs/file sample-path  (:sample-input-name CONSTANTS))
        manual-path (utils/new-root-path (:samples-root ROOTS) (fs/path (:samples-root ROOTS) "manual") sample-input)]
    )
  )

(defn copy-sample-images
  [model-record])

(defn populate-model-folder
  "Downloads all needed urls from the model-record into local files.
  Copies the rdf and the tiffs from local folders."
  ([model-record]
   (populate-model-folder model-record MODEL-DIR))
  ([model-record model-dir-name]
   (let [folder-file (get-destination-folder model-record model-dir-name)
         sample-ims (fs/glob (get-in model-record [:paths :samples-path]) "*.tif")
         copy-opts {:replace-existing true}]
     ; create folder if it didn't exist
     (fs/create-dirs folder-file)
     ; copy rdf.yaml
     (fs/copy (get-in model-record [:paths :rdf-path]) (fs/file folder-file "rdf.yaml") copy-opts)
     ;copy tiff images
     (doall (map #(fs/copy % (fs/file folder-file (fs/file-name %)) copy-opts) sample-ims)))))

; (my-time (populate-model-folder (second @model-records)))
; with (doall (map
; => :return (117802198 4355 699), :iso "PT39.381S"
; with (doall (pmap
; => :return (117802198 4355 699), :iso "PT33.944S"
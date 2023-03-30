(ns reproduce.setup-fiji
  "Download and install a blank Fiji for testing"
  (:require [config :refer [CONSTANTS FILES]]
            [utils]
            [downloads.download :as download]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [babashka.fs :as fs]))
; todo setup deepimagej 2.15
; todo setup deepimagej 3.0 (needs actual urls)


(def fiji-zip-os-names
  "name of the fiji zip depends on the OS"
  {"Windows" "fiji-win64.zip"
   "Linux"   "fiji-linux64.zip"
   "MAC"     "fiji-macosx.zip"})

(def fiji-zip-name
  (last (first (filter (fn [[k v]] (str/includes? (System/getProperty "os.name") k))
                       fiji-zip-os-names))))

(def fiji-url (str (:fiji-download-url CONSTANTS) fiji-zip-name))

(defn download-zip
  [url destination-folder-file zip-name]
  (let [timed-response (utils/my-time (:body (download/get-url-response url)))]
    (download/save-file-with-info destination-folder-file timed-response zip-name)))

(defn unzip-pipeline
  "1. download zip
   2. make sure wanted final location is free (optional)
   3. unzip in wanted final location
   4. delete zip"
  ([url zip-name unzip-destination clean-destination?]
   (unzip-pipeline url zip-name unzip-destination clean-destination? (fs/home)))
  ([url zip-name unzip-destination clean-destination? download-destination]
   (download-zip url download-destination zip-name)
   (if (and clean-destination? (fs/exists? unzip-destination))
     (fs/delete-tree unzip-destination))
   (fs/unzip (fs/file download-destination zip-name) unzip-destination)
   (fs/delete-if-exists (fs/file download-destination zip-name))
   (println "Unzipped successfully" zip-name "in" unzip-destination)))

(defn setup-fiji-&-deepimagej
  []
  (unzip-pipeline fiji-url fiji-zip-name (:fiji-home FILES) true)
  (unzip-pipeline (:dij2-deps-url CONSTANTS) (download/get-url-filename (:dij2-deps-url CONSTANTS))
                  (fs/file (:fiji-home FILES) "Fiji.app" "jars") false)
  (download-zip (:dij2-download-url CONSTANTS) (fs/file (:fiji-home FILES) "Fiji.app" "plugins")
                (download/get-url-filename (:dij2-download-url CONSTANTS))))

; trying each part
(comment
  (download-zip fiji-url (fs/home) fiji-zip-name)
  )

(comment
  (fs/unzip (fs/file (fs/home) fiji-zip-name)
         (fs/file (fs/home) "just-testing"))
  )

(comment
  (unzip-pipeline fiji-url fiji-zip-name (fs/file (fs/home) "just_testing") true)
  (unzip-pipeline fiji-url fiji-zip-name (:fiji-home FILES) true)
  (unzip-pipeline (:dij2-deps-url CONSTANTS) (download/get-url-filename (:dij2-deps-url CONSTANTS))
                  (fs/file (:fiji-home FILES) "Fiji.app" "jars") false)
  (download-zip (:dij2-download-url CONSTANTS) (fs/file (:fiji-home FILES) "Fiji.app" "plugins")
                (download/get-url-filename (:dij2-download-url CONSTANTS)))
  )

(ns reproduce.setup-fiji
  "Download and install a blank Fiji for testing DeepImageJ-plugin"
  (:require [config :refer [CONSTANTS FILES]]
            [utils]
            [downloads.download :as download]
            [babashka.fs :as fs]))

(def fiji-url (str (:fiji-download-url CONSTANTS) (:fiji-zip-name CONSTANTS)))

(defn unzip-pipeline
  "1. download zip
   2. make sure wanted final location is free (optional)
   3. unzip in wanted final location
   4. delete zip"
  ([url zip-name unzip-destination clean-destination?]
   (unzip-pipeline url zip-name unzip-destination clean-destination? (fs/home)))
  ([url zip-name unzip-destination clean-destination? download-destination]
   (download/download-file url download-destination zip-name)
   (if (and clean-destination? (fs/exists? unzip-destination))
     (fs/delete-tree unzip-destination))
   (fs/unzip (fs/file download-destination zip-name) unzip-destination)
   (fs/delete-if-exists (fs/file download-destination zip-name))
   (println "Unzipped successfully" zip-name "in" unzip-destination)))

(defn setup-fiji-&-deepimagej
  "Sets up fiji and deepimagej in a given folder, the default is the one defined in (:fiji-home FILES)"
  ([] (setup-fiji-&-deepimagej (:fiji-home FILES)))
  ([fiji-home]
   (unzip-pipeline fiji-url (:fiji-zip-name CONSTANTS) fiji-home true)
   (unzip-pipeline (:dij2-deps-url CONSTANTS) (download/get-url-filename (:dij2-deps-url CONSTANTS))
                   (fs/file fiji-home "Fiji.app" "jars") false)
   (download/download-file (:dij2-download-url CONSTANTS) (fs/file fiji-home "Fiji.app" "plugins")
                  (download/get-url-filename (:dij2-download-url CONSTANTS)))))

(defn setup-fiji-&-deepimagej3
  ([] (setup-fiji-&-deepimagej (:fiji-home FILES)))
  ([fiji-home]
   (unzip-pipeline fiji-url (:fiji-zip-name CONSTANTS) fiji-home true)
   ; if deps are only 1 jar, use download-file (not unzip-pipeline)
   (unzip-pipeline (:dij3-deps-url CONSTANTS) "dij3-deps.zip" (fs/file fiji-home "Fiji.app" "jars") false)
   (download/download-file (:dij3-download-url CONSTANTS) (fs/file fiji-home "Fiji.app" "plugins")
                           "DeepImageJ_-3.0.0.jar")))

; trying each part
(comment
  (download/download-file fiji-url (fs/home) "my.zip")
  )

(comment
  (fs/unzip (fs/file (fs/home) "my.zip") (fs/file (fs/home) "just-testing"))
  )

(comment
  (setup-fiji-&-deepimagej (fs/file (fs/home) "just_testing_3"))
  (unzip-pipeline fiji-url (:fiji-zip-name CONSTANTS) (fs/file (fs/home) "just_testing") true)
  (unzip-pipeline fiji-url (:fiji-zip-name CONSTANTS) (:fiji-home FILES) true)
  (unzip-pipeline (:dij2-deps-url CONSTANTS) (download/get-url-filename (:dij2-deps-url CONSTANTS))
                  (fs/file (:fiji-home FILES) "Fiji.app" "jars") false)
  (download/download-file (:dij2-download-url CONSTANTS) (fs/file (:fiji-home FILES) "Fiji.app" "plugins")
                 (download/get-url-filename (:dij2-download-url CONSTANTS)))

  (setup-fiji-&-deepimagej3 (fs/file (fs/home) "drive_testing_3"))
  )

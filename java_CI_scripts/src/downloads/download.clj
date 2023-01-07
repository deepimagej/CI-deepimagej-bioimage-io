(ns downloads.download
  (:require [babashka.curl :as curl]))

(defn get-url->byte-arr
  "Gets the contents of a url as a byte array"
  [url]
  (let [basic-opts {:as :bytes :throw false},
        opts (if (= "Windows 10" (System/getProperty "os.name"))
               (assoc basic-opts :compressed false)
               basic-opts)]
    (curl/get url opts)))
; command line argument for fiji scripts needs this
; source 1: https://imagej.net/scripting/headless
; source 2: https://imagej.net/scripting/parameters
#@ String (label="", value=".") folder ; can access by user/folder

(ns reproduce.test-1-with-deepimagej
  "Requires the information of the models to be in their model folder in the file dij_args.edn "
  (:require [clojure [edn :as edn] [test :refer :all] [pprint :as ppr]]
            [clojure.java [io :refer [file make-parents copy delete-file]]])
  (:import [ij IJ])
  )

(println "the folder is" user/folder)

(def COMM-FILE (file user/folder "dij_args.edn"))
(def DIJ-MODEL (edn/read-string (slurp COMM-FILE)))
(def FIJI-HOME (file (System/getProperty "user.home") "blank_fiji"))
(def MODEL-DIR-NAME "the_model")
(def OUTPUT-NAME "CI_OUTPUT.tif")



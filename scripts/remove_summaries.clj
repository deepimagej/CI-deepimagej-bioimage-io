#! /usr/bin/env bb

(ns remove-summaries 
  "Delete all test_summary.yaml from git version control"
  (:require [babashka.fs :as fs]
            [babashka.process :as pr]))

(def files (fs/glob "." "**/test_summary.yaml"))

(printf "Number of test summaries %d\n" (count files))
(apply pr/shell "git" "rm" (map str files))

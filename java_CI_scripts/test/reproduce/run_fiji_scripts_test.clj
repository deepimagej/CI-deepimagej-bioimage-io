(ns reproduce.run-fiji-scripts-test
  (:require [babashka.fs :as fs]
            [config :refer [FILES CONSTANTS]]
            [reproduce.run-fiji-scripts :refer :all]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(deftest quote-arg-test
  (let [expected (if (fs/windows?)
                   (str \" (:fiji-scripts-arg-name CONSTANTS) "=" \' 111 \' \")
                   (str \' (:fiji-scripts-arg-name CONSTANTS) "=" \" 111 \" \'))]
    (is (= expected (quote-arg 111)))))


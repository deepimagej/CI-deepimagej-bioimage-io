(ns reproduce.run-fiji-scripts-test
  (:require [config :refer [FILES CONSTANTS]]
            [reproduce.run-fiji-scripts :refer :all]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(deftest quote-arg-test
  (let [expected (if (str/includes? (System/getProperty "os.name") "Windows")
                   (str \" (:fiji-scripts-arg-name CONSTANTS) "=" \' 111 \' \")
                   (str \' (:fiji-scripts-arg-name CONSTANTS) "=" \" 111 \" \'))]
    (is (= expected (quote-arg 111)))))

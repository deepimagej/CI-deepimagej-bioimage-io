(ns reproduce.run-fiji-scripts-test
  (:require [reproduce.run-fiji-scripts :refer :all]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(deftest quote-arg-test
  (let [expected (if (str/includes? (System/getProperty "os.name") "Windows")
                   (str \" fiji-arg-name "=" \' 111 \' \")
                   (str \' fiji-arg-name "=" \" 111 \" \'))]
    (is (= expected (quote-arg 111)))))

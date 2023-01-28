(ns config-test
  (:require [config :refer :all]
            [clojure.test :refer :all]))

(deftest absolutize-nested-test
  (is (= [:out :err] (keys (absolutize-nested (:log-files FILES)))))
  (let [shape {:config-file "" :log-files {:out "" :err ""}}
        result (absolutize-nested (select-keys FILES [:config-file :log-files]))]
    (is (= [:config-file :log-files] (keys result)))
    (is (= [:out :err] (keys (:log-files result))))))

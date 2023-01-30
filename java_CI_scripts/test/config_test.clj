(ns config-test
  (:require [config :refer :all]
            [clojure.test :refer :all]))

(deftest absolutize-nested-test
  (is (= [:out :err] (keys (absolutize-nested (:logs FILES)))))
  (let [shape {:config "" :logs {:out "" :err ""}}
        result (absolutize-nested (select-keys FILES [:config :logs]))]
    (is (= [:config :logs] (keys result)))
    (is (= [:out :err] (keys (:logs result))))))

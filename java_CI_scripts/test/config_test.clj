(ns config-test
  (:require [config :refer :all]
            [clojure.test :refer :all]
            [clojure.edn :as edn]
            [babashka.fs :as fs]))

(deftest absolutize-nested-test
  (is (= [:out :err] (keys (absolutize-nested (:logs FILES)))))
  (let [shape {:config "" :logs {:out "" :err ""}}
        result (absolutize-nested (select-keys FILES [:config :logs]))]
    (is (= [:config :logs] (keys result)))
    (is (= [:out :err] (keys (:logs result))))))

(deftest serialize-config-test
  (let [t-config (fs/file "." "test_config.edn")
        _ (serialize-config t-config)
        m (edn/read-string (slurp t-config))]
    (is (fs/exists? t-config))
    (is (>= (.length t-config) 1325))
    (is (= (:fiji-flags m) ["--headless" "--ij2" "--console" "--run"]))
    (is (= m (merge CONSTANTS (absolutize-nested ROOTS) (absolutize-nested FILES))))
    (is (fs/delete-if-exists t-config))))
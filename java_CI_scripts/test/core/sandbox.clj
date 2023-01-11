(ns core.sandbox
  (:require collection
            models
            [downloads download initial-checks]
            summaries
            [reproduce communicate]
            [core main]
            [test-setup :refer :all]
            [clojure.test :refer :all]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths load-model-records  load-rdfs-parsed)

(deftest dummy-test
  (testing "dummy test to load everything form test-setup"))
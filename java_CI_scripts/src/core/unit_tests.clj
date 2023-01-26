(ns core.unit-tests
  "Require and run all tests for this project"
  (:require [clojure.test :refer [run-tests]]))

(def nss-to-test
  "Names of the namespaces to test"
  ['collection-test
   'models-test
   'summaries.summary-test
   'downloads.initial-checks-test 'downloads.download-test 'downloads.p-process-test
   'reproduce.communicate-test 'reproduce.run-fiji-scripts-test
   'core.cli-test])

; All the namespaces need first to be required
(apply require nss-to-test)

(defn run-all-tests []
  (apply run-tests nss-to-test))
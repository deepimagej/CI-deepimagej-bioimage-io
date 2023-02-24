(ns core.unit-tests
  "Require and run all unit tests for this project"
  (:require [clojure.test :refer [run-tests]]
            [utils]))

(def nss-to-test
  "Names of the namespaces to test"
  ['collection-test
   'models-test
   'utils-test
   ; discriminate tests should go after reproduce-checks for the fake files to be written
   'summaries.summary-test 'summaries.reproduce-checks-test 'summaries.errors-test
   'summaries.init-checks-test 'summaries.discriminate-test
   'downloads.download-test 'downloads.p-process-test
   'reproduce.communicate-test 'reproduce.run-fiji-scripts-test
   'core.cli-test
   'config-test])

; All the namespaces need first to be required
(apply require nss-to-test)

(defn run-all-tests []
  (let [timed (utils/my-time (apply run-tests nss-to-test))]
    (println "Time Taken for all unit tests: " (:iso timed))
    (:return timed)))
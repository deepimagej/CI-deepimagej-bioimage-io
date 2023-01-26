(ns core.unit-tests
  "Require and run all tests for this project"
  (:require collection-test
            models-test
            [summaries summary-test]
            [downloads initial-checks-test download-test p-process-test]
            [reproduce communicate-test run-fiji-scripts-test]
            [core cli-test]
            [clojure.test :refer [run-tests]]))

; todo imporvement: run tests on all symbols required (to not type twice)
; (apply require ['collection-test 'models-test])
; (apply run-tests ['collection-test 'models-test])

(defn run-all-tests []
  (run-tests 'collection-test
             'models-test
             'summaries.summary-test
             'downloads.initial-checks-test 'downloads.download-test 'downloads.p-process-test
             'reproduce.communicate-test 'reproduce.run-fiji-scripts-test
             'core.cli-test))
(ns core.main
  (:require collection
            models

            [core cli actions unit-tests]
            [babashka.fs :as fs]))



(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (core.cli/validate-args args)]
    (if exit-message
      (core.cli/exit (if ok? 0 1) exit-message)
      (cond
        (:unit-test options)
        (core.unit-tests/run-all-tests)
        (:json-string options)
        (core.actions/download-pipeline :json-string options)
        :else
        (core.actions/download-pipeline :json-file options)))))
(ns downloads.p-process)

(defn get-p*processing-names
  "Gets list with the names of the p*processing scripts needed by the model record"
  [model-record]
  (filter #(not (nil? %)) (map :script (:p*process model-record))))

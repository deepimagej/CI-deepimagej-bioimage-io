(ns summaries.discriminate
  "Sieve the models with their respective error")

;  DATA STRUCTURE FOR DISCRIMINATED MODELS
(comment
  {:keep-testing [list-of-model-rp]
   :error-found  {:error-key1 [list-of-model-rp]
                  :error-key2 [list-of-model-rp]}})

; todo, put this somewhere different, used also for final-checks
(defn check-error
  "Adds results of checking a new error to the data structure for discriminated models.
  To be used as reducing function when iterating over all possible errors"
  [discriminated-models [error-key discriminating-fn]]
  (let [{to-keep true, with-error false} (group-by discriminating-fn (:keep-testing discriminated-models))]
    (hash-map :keep-testing to-keep
              :error-found (assoc (:error-found discriminated-models) error-key with-error))))

(defn separate-by-error
  "Discriminative function should return true to keep testing, false if error occurred
  After an error happens, no more error checks are made for a model"
  [models-list error-fns]
  (reduce check-error {:keep-testing models-list} error-fns))

; Deprecated, but not deleted. It can clarify how things worked initially
(defn separate-by-dij-config
  "DEPRECATED Separates models into the ones that have or not-have deepimagej config field in the rdf.
  Deprecated by using generic approach to all error types"
  [models]
  (clojure.set/rename-keys (group-by :dij-config? (map :rdf-info models)) {true :keep-testing false :no-dij-config}))
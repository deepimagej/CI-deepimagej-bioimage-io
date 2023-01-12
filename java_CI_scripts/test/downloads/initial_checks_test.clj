(ns downloads.initial-checks-test
  (:require [downloads.initial-checks :refer :all]
            [test-setup :refer :all]
            [clojure.test :refer :all]))

(def model-rp's
  "list of records and parsed rdf for every tests model"
  (atom nil))
(def all-model-rp's
  "list of records and parsed rdf for all models"
  (atom nil))

(defn load-model-rp's
  [test-fn]
  (reset! model-rp's (map #(map->ModelRP {:model-record %1 :parsed-rdf %2}) @model-records @rdfs-parsed))
  (reset! all-model-rp's (map #(map->ModelRP {:model-record %1 :parsed-rdf %2}) @all-model-records @all-rdfs-parsed))
  (test-fn))

(use-fixtures :once load-test-paths load-rdfs-parsed load-model-records load-model-rp's)

(defn count-dict
  "counts dictionary entries that are seqs"
  [d] (reduce (fn [m [k v]] (assoc m k (count v))) {}  d))

(deftest count-dict-test
  (is (= (count-dict {:a [1 2] :b [4 5 6]}) {:a 2 :b 3})))

; todo: raplace this with generalized version
(deftest separate-by-dij-config-test
  (testing "The 3 models tf, pt and state-dict (incompatible with dij)"
    (let [separated (separate-by-dij-config @model-records)]
      (is (= (keys separated) [:keep-testing :no-dij-config]))
      (is (= 2 (count (:keep-testing separated))))
      (is (= 1 (count (:no-dij-config separated))))))
  (testing "All models in the collection"
    (let [separated (separate-by-dij-config @all-model-records)]
      (is (= {:keep-testing 47 :no-dij-config 117} (count-dict separated))))))


(deftest dij-config?-test
  (is (= 2 (count (filter dij-config? @model-rp's))))
  (is (= 47 (count (filter dij-config? @all-model-rp's)))))

(deftest no-run-mode?-test
  (let [{without-rm true with-rm false} (group-by no-run-mode? @all-model-rp's)]
    (is (= 160 (count without-rm)))
    (is (= 4 (count with-rm)))
    (is (= (repeat 4 "deepimagej")
           (mapv #(get-in % [:parsed-rdf :run_mode :name]) with-rm))))
  )

(deftest check-error-test
  (let [discriminated (check-error {:keep-testing @model-records} (first error-functions))])
  )

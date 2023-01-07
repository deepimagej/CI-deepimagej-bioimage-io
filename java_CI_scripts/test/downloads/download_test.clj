(ns downloads.download-test
  (:require [downloads.download :refer :all]
            [test-setup :refer [load-test-paths load-model-records model-records]]
            [clojure.test :refer :all]))

(use-fixtures :once load-test-paths load-model-records)

(deftest my-time-test
  (let [time-map (my-time (do (Thread/sleep 1000)
                              (apply + (take 5 (range 1 100 2)))))
        iso-str (:iso time-map)]
    (is (= (* 5 5) (:return time-map)))
    (is (>= 1 (.getSeconds (:duration time-map))))
    (is (<= (.getNano (:duration time-map)) 1e9) "time taken is 1s and a little bit more (not 2s)")
    (is (= [iso-str "1" "S"] (re-matches #"PT(\d).+\d+(S)" iso-str)))))

(deftest get-url->byte-arr-test
  (let [url-1 "https://yaml.org/"
        ;ulr-1 (:source (first (:weights (first @model-records)))) ; 5Mb too big for fast tests
        ulr-2 "https://zenodo.org/does-not-exist"
        url-3 "not even an url..."]
    (testing "A valid url"
      (let [response (get-url->byte-arr url-1)]
        (is (= 200 (:status response)))
        (is (= "" (:err response)))
        (is (= 0 (:exit response)))
        (is (= 19586 (alength (:body response))) "This will fail if yaml.org web is changed"))))
  )

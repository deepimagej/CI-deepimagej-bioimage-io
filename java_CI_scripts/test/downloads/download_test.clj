(ns downloads.download-test
  (:require [downloads.download :refer :all]
            [test-setup :refer [load-test-paths load-model-records an-edn model-records]]
            [clojure [test :refer :all] [edn :as edn]]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths load-model-records)

(deftest my-time-test
  (let [time-map (my-time (do (Thread/sleep 1000)
                              (apply + (take 5 (range 1 100 2)))))
        iso-str (:iso time-map)]
    (is (= (* 5 5) (:return time-map)))
    (is (>= 1 (.getSeconds (:duration time-map))))
    (is (<= (.getNano (:duration time-map)) 1e9) "time taken is 1s and a little bit more (not 2s)")
    (is (= [iso-str "1" "S"] (re-matches #"PT(\d).+\d+(S)" iso-str)))))

(deftest get-url-response-test
  (let [url-1 "https://yaml.org/"
        ;ulr- (:source (first (:weights (first @model-records)))) ; 5Mb too big for fast tests
        url-2 "https://zenodo.org/does-not-exist"
        url-3 "https://zzzzzz.org/does-not-exist"
        url-4 33
        url-5 (:url an-edn)]
    (testing "A valid url"
      (let [response (get-url-response url-1)]
        (is (= 200 (:status response)))
        (is (= "" (:err response)))
        (is (zero? (:exit response)))
        (is (= 19586 (alength (:body response))) "This will fail if yaml.org web is changed")))
    (testing "An url that does not exist"
      (let [response (get-url-response url-2)]
        (is (= 404 (:status response)))
        (is (= "" (:err response)))
        (is (> (alength (:body response)) 0))))
    (testing "host does not exist"
      (let [response (get-url-response url-3)]
        (is (nil? (:status response)))
        (is (> (count (:err response)) 0))
        (is (= 6 (:exit response)))
        (is (zero? (alength (:body response))))))
    (testing "type of arg is not string"
      (let [response (get-url-response url-4)]
        (is (zero? (alength (:body response))))
        (is (= 3 (:exit response)))))
    (testing "An edn file from github"
      (let [body (:body (get-url-response url-5))
            parsed (edn/read-string (slurp body))]
        (is (= 145 (alength body)))
        (is (= parsed (dissoc an-edn :url)))))))

(deftest get-url-filename-test
  (is (= "an.edn" (get-url-filename (:url an-edn))))
  (is (= "confocal_pnas_2d.pytorch"
         (get-url-filename (:source (first (:weights (first @model-records))))))))

(deftest byte-arr->file!-test
  (let [arrays {:ba-1 (:body (get-url-response (:url an-edn)))
                :ba-2 (.getBytes "abcd")
                ; download an image
                :ba-3 (:body (get-url-response "https://www.uc3m.es/ss/Satellite?blobcol=urldata&blobkey=id&blobtable=MungoBlobs&blobwhere=1371552339304&ssbinary=true"))}
        toy-dir (fs/path "out" "test" "toy_folder")
        gen-filename (fn [kwd] (str (name kwd) ".file"))]
    ; create the parent folder if it doesn't exist
    (fs/create-dirs toy-dir)
    ; write byte arrays in files
    (doall (map (fn [[k v]] (byte-arr->file! v toy-dir (gen-filename k))) arrays))
    ; testing
    (is (= (edn/read-string (slurp (fs/file toy-dir (gen-filename :ba-1))))
           (dissoc an-edn :url)))
    (is (= "abcd" (slurp (fs/file toy-dir (gen-filename :ba-2)))))
    (is (= 4938 (.length (fs/file toy-dir (gen-filename :ba-3)))))
    ))

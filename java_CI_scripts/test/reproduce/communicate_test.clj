(ns reproduce.communicate-test
  (:require [reproduce.communicate :refer :all]
            [test-setup :refer [load-test-paths load-model-records model-records]]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths load-model-records)

(deftest format-axes-test
  (is (= (format-axes "byxzc") "Y,X,Z,C")))

(deftest format-tiles-test
  (is (= (format-tiles "1 x 256 x 256 x 8 x 1") "256,256,8,1")))

(deftest weight-format-test
  (is (= (weight-format (nth @model-records 1)) "Tensorflow"))
  (is (= (weight-format (nth @model-records 2)) "Pytorch"))
  (is (= (weight-format (nth @model-records 0)) nil)))

(deftest get-pprocess-test
  (is (= (get-pprocess :post-p (nth @model-records 2)) "no postprocessing"))
  (is (= (get-pprocess :pre-p (nth @model-records 1)) "per_sample_scale_range.ijm")))

(deftest build-dij-arg-test
  (is (= (build-dij-arg (nth @model-records 1))
         (map->DijArg {:model "Cell Segmentation from Membrane Staining for Plant Tissues",
                       :format "Tensorflow",
                       :preprocessing "per_sample_scale_range.ijm",
                       :postprocessing "binarize.ijm",
                       :axes "Y,X,Z,C",
                       :tile "256,256,8,1",
                       :logging "Normal"}))))

(deftest bracketize-test
  (is (= "[abc]" (bracketize "abc"))))

(deftest dij-arg-str-test
  (is (= (dij-arg-str (nth @model-records 1))
         "model=[Cell Segmentation from Membrane Staining for Plant Tissues] format=[Tensorflow] preprocessing=[per_sample_scale_range.ijm] postprocessing=[binarize.ijm] axes=Y,X,Z,C tile=256,256,8,1 logging=Normal")))

(deftest get-name-from-url-test
  (is (= (get-name-from-url "https://zenodo.org/my-file.txt") "my-file.txt")))

(deftest get-test-images-test
  (is (= (get-test-images (nth @model-records 1))
         {:inputs "exampleImage.tif", :outputs "resultImage.tif"}))
  (is (= (get-test-images (nth @model-records 2))
         {:inputs "sample_input_0.tif", :outputs "sample_output_0.tif"})))

(deftest get-model-folder-test
  (let [folder (get-model-folder (nth @model-records 1))]
    (is (nil? (str/index-of folder "\\")))
    (is (= (last folder) \/))))

; no test for (build-dij-model) because it is just a record, all fields are tested previously

(deftest write-comm-file-test
  (let [dij-models (map build-dij-model (rest @model-records))
        c-name (fs/file-name comm-file)
        c-file (fs/file "." c-name)
        c-file-not-in-dir? (fn [root name]
                         (empty? (filter #(= % name) (map fs/file-name (fs/list-dir root)))))]
    (testing "Before test, see that no file is in the directory"
      (is (c-file-not-in-dir? "." c-name)))
    (testing "File written with correct contents"
      (write-comm-file dij-models c-file)
      (is (not (c-file-not-in-dir? "." c-name)) "File exists now in the directory")
      (let [[map1 map2 :as all] (edn/read-string (slurp c-file))]
        (is (= (type all) clojure.lang.PersistentVector))
        (is (>= 2 (count all)))
        (is (:nickname map1) "humorous-owl")
        (is (:output-img map2) "sample_output_0.tif")))
    (testing "After test, delete existing file"
      (is (fs/delete-if-exists c-file))
      (is (c-file-not-in-dir? "." c-name)))))

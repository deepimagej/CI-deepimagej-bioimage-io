(ns reproduce.communicate-test
  (:require
    [reproduce.communicate :refer :all]
    [downloads-test :refer [model-records load-model-records]]
    [clojure.test :refer :all]
    [clojure.string :as str]))

(use-fixtures :once load-model-records)

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

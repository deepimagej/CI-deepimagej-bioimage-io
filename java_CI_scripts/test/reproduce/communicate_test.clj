(ns reproduce.communicate-test
  (:require [config :refer [FILES]]
            [reproduce.communicate :refer :all]
            [test-setup :refer [load-test-paths load-model-records model-records all-model-records]]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [babashka.fs :as fs]))

(use-fixtures :once load-test-paths load-model-records)

(deftest format-axes-test
  (is (= (format-axes "byxzc") "Y,X,Z,C")))

(deftest get-input-shape-test
  (is (thrown? java.io.FileNotFoundException (get-input-shape (first @model-records))))
  (is (= [1 256 256 8 1] (get-input-shape (second @model-records))))
  (is (= [1 1 32 360 360] (get-input-shape (last @model-records)))))

(deftest format-tiles-test
  (is (= (format-tiles-str "1 x 256 x 256 x 8 x 1") "256,256,8,1"))
  (is (= (format-tiles-str "360 x 360 x 32 x 1") "360,360,32,1"))
  (is (= (format-tiles [1 256 256 8 1]) "256,256,8,1")))

(deftest weight-format-test
  (is (= (weight-format (nth @model-records 1)) "Tensorflow"))
  (is (= (weight-format (nth @model-records 2)) "Pytorch"))
  (is (= (weight-format (nth @model-records 0)) nil)))

(deftest get-p*process-test
  (is (= (get-p*process :post-p (nth @model-records 2)) "no postprocessing"))
  (is (= (get-p*process :pre-p (nth @model-records 1)) "per_sample_scale_range.ijm")))

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
  (are [expected input] (= expected (bracketize input))
                        "abc" "abc"
                        "[with many spaces]" "with many spaces"
                        "[with_underscores_]" "with_underscores_"))

(deftest dij-arg-str-test
  (is (= (dij-arg-str (nth @model-records 1))
         "model=[Cell Segmentation from Membrane Staining for Plant Tissues] format=Tensorflow preprocessing=[per_sample_scale_range.ijm] postprocessing=binarize.ijm axes=Y,X,Z,C tile=256,256,8,1 logging=Normal")))

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
        c-name (fs/file-name (:models-vector FILES))
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

(deftest write-dij-model-test
  (let [model-folders (map #(get-in % [:paths :model-dir-path]) (rest @model-records))]
    (mapv fs/create-dirs model-folders)
    (mapv write-dij-model (rest @model-records))
    (let [dij-models (map #(into {} (build-dij-model %)) (rest @model-records))
          files (map #(fs/file (get-in % [:paths :model-dir-path]) "dij_args.edn") (rest @model-records))
          parsed-dicts (map #(edn/read-string (slurp %)) files)]
      (is (= (first parsed-dicts) (first dij-models)))
      (is (= (last parsed-dicts) (last dij-models))))))

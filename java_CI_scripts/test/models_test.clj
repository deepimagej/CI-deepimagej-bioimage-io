(ns models-test
  (:require [models :refer :all]
            [summaries.summary :refer [SUMMA-ROOT]]
            [test-setup :refer [rdf-paths load-test-paths]]
            [clojure.test :refer :all]
            [clojure.java.io :refer [as-url]]
            [babashka.fs :as fs]))

; TODO use rdf-parsed from test-setup
(def model-dicts {:a-model (atom nil) :tf-model (atom nil) :pt-model (atom nil)})

(defn load-test-models
  "Parses rdfs of test paths into different models for testing:
  - A model without dij config (with state dict)
  - A tensorflow model (with keras) with pre- and post- process
  - A torchscript model (with state dict) with pre-process"
  [test-fn]
  (reset! (:a-model model-dicts) (parse-model @(:an-rdf rdf-paths)))
  (reset! (:tf-model model-dicts) (parse-model @(:tf-rdf rdf-paths)))
  (reset! (:pt-model model-dicts) (parse-model @(:pt-rdf rdf-paths)))
  (test-fn))

(use-fixtures :once load-test-paths load-test-models)

(deftest parse-model-test
  (is (= "laid-back-lobster" (get-in @(:a-model model-dicts) [:config :bioimageio :nickname])))
  (is (= "13bbeb9a2403f5ff840951d8907586cf1ceded3072d36466db3e592b5ad53649"
         (get-in @(:a-model model-dicts) [:weights :pytorch_state_dict :sha256]))))

(deftest get-rdf-info-test
  (is (= (into {} (get-rdf-info @(:tf-model model-dicts)))
         {:type "model", :dij-config? true, :run-mode nil})))

(deftest get-weight-info-test
  (let [w (get-weight-info @(:a-model model-dicts))
        w1 (get-weight-info @(:tf-model model-dicts))
        w2 (get-weight-info @(:pt-model model-dicts))
        w3 (get-weight-info :torchscript @(:tf-model model-dicts))]
    (testing "rdf with 1 incompatible weight (pytorch state dict)"
      (is (= 1 (count w)))
      (is (= (->Weight nil "https://zenodo.org/api/files/4a69ddca-3874-4469-b11d-b9ed626d197f/confocal_pnas_2d.pytorch")
             (first w))))
    (testing "rdf with Tensorflow weights"
      (is (= 2 (count w1)))
      (is (nil? (:type (first w1))))
      (is (= "Tensorflow" (:type (second w1)))))
    (testing "rdf with Pytorch weights"
      (is (= 2 (count w2)))
      (is (nil? (:type (first w2))))
      (is (= "Pytorch" (:type (second w2)))))
    (testing "rdf with incorrect keyword"
      (is (= w3 (->Weight "Pytorch" nil))))))

(deftest get-p*process-info-test
  (let [p (get-p*process-info @(:a-model model-dicts))
        p1 (get-p*process-info @(:tf-model model-dicts))
        pp-dict-2 (get-in @(:pt-model model-dicts) [:config :deepimagej :prediction])]
    (testing "rdf with no preprocessing"
      (is (= [] (get-p*process-info p))))
    (testing "rdf with pre- and post-processing"
      (is (= (set p1) (set [(->PProcess :post-p "binarize.ijm")
                            (->PProcess :pre-p "per_sample_scale_range.ijm")]))))
    (testing "rdf with only pre-processing"
      (is (= (get-p*process-info :postprocess pp-dict-2)
             (->PProcess :post-p nil)))
      (is (= (get-p*process-info :preprocess pp-dict-2)
             (->PProcess :pre-p "zero_mean_unit_variance.ijm"))))))

(deftest get-tensor-info-test
  (let [[[a-in a-o] [b-in b-o] [c-in c-o]] (map (fn [[k v]] (get-tensor-info @v)) model-dicts)]
    (testing "A model dict without DeepImageJ config"
      (is (= (:name a-in) "raw"))
      (is (nil? (:sample a-in)))
      (is (= (:axes a-o) "bczyx"))
      (is (nil? (:shape a-o))))
    (testing "A tensorflow model"
      (let [in-url (as-url (:sample b-in))
            in-path (fs/path (.getPath in-url))
            out-url (as-url (:sample b-o))
            out-path (fs/path (.getPath out-url))]
        (is (= "exampleImage.tif" (fs/file-name in-path)))
        (is (= "1 x 256 x 256 x 8 x 1" (:shape b-in)))
        (is (= "resultImage.tif" (fs/file-name out-path)))
        (is (= "zenodo.org" (.getHost out-url)))))
    (testing "A pytorch model"
      (is (= (:name c-in) "input0"))
      (is (= (:shape c-o) "360 x 360 x 32 x 1")))))

; filter list
; (filter #(= (:type %) :inputs) b)
; (filter (fn [t] (= (:type t) :inputs)) b)
; (filter (fn [{t :type}] (= t :inputs)) b) ;deconstructing
; (filter (fn [{:keys [:type]}] (= type :inputs)) b)

(deftest gen-model-path-test
  (let [expected-path (fs/path MODEL-ROOT "10.5281" "zenodo.6334881" "6346477")]
    (is (= (str (gen-model-path @(:an-rdf rdf-paths))) (str expected-path)))))

; No test for create-model-dir
; too similar to the summary one (which is a big test)
; it is tested seeing that the models are created (bash 'tree' commands after running core.-main)

(deftest get-paths-info-test
  (let [test-rdf @(:pt-rdf rdf-paths)
        paths (get-paths-info test-rdf)
        components '("10.5281" "zenodo.5874741" "5874742")
        s-path (apply fs/path (conj components SUMMA-ROOT))
        m-path (apply fs/path (conj components MODEL-ROOT))
        sample-path (apply fs/path (conj components SAMPLE-ROOT))]
    (is (= [:rdf-path :summa-path :model-dir-path :samples-path] (keys paths)))
    (is (= (:rdf-path paths) test-rdf))
    (is (= (str s-path) (str (:summa-path paths))))
    (is (= (str m-path) (str (:model-dir-path paths))))
    (is (= (str sample-path) (str (:samples-path paths))))
    (is (= paths (->Paths test-rdf s-path m-path sample-path))) "Equality regardless different references"))

(deftest build-model-test
  (testing "Only the new fields (record fields already tested"
    (let [the-keys [:name :nickname :dij-config? :attach]
          vals-model-0 (map #(% (build-model @(:an-rdf rdf-paths))) the-keys)
          vals-model-tf (map #(% (build-model @(:tf-rdf rdf-paths))) the-keys)]
      (is (= (butlast vals-model-0) ["2D UNet Arabidopsis Apical Stem Cells" "laid-back-lobster" false]))
      (is (nil? (last vals-model-0)))
      (is (= (butlast vals-model-tf) ["Cell Segmentation from Membrane Staining for Plant Tissues" "humorous-owl" true]))
      (is (last vals-model-tf) ["https://zenodo.org/api/files/eb8f4259-001c-4989-b8ea-d2997918599d/exampleImage.tif"
                                "https://zenodo.org/api/files/eb8f4259-001c-4989-b8ea-d2997918599d/resultImage.tif"
                                "https://zenodo.org/api/files/eb8f4259-001c-4989-b8ea-d2997918599d/per_sample_scale_range.ijm"
                                "https://zenodo.org/api/files/eb8f4259-001c-4989-b8ea-d2997918599d/binarize.ijm"
                                "https://zenodo.org/api/files/eb8f4259-001c-4989-b8ea-d2997918599d/params.csv"]))))



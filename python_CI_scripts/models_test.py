from config import ROOTS
import utils

import collection_test
import models

from pathlib import Path

# test parse_model
parsed_model = models.parse_model(collection_test.r_c[0])

assert utils.get_in(parsed_model, ["config", "bioimageio", "nickname"]) == "humorous-owl"

# test rdf_info
rdf_info = models.get_rdf_info(parsed_model)

assert rdf_info["type"] == "model"
assert rdf_info["dij-config?"]
assert rdf_info["run-mode"] is None

an_rdf = Path(ROOTS["collection-root"], "10.5281", "zenodo.6334881", "6346477", "rdf.yaml")
tf_rdf = Path(ROOTS["collection-root"], "10.5281", "zenodo.5749843", "5888237", "rdf.yaml")
pt_rdf = Path(ROOTS["collection-root"], "10.5281", "zenodo.5874741", "5874742", "rdf.yaml")
no_dij_config_rdf = Path(ROOTS["collection-root"], "10.5281", "zenodo.7274275", "8123818", "rdf.yaml")

not_a_model_rdf = Path(ROOTS["collection-root"], "bioimageio", "qupath", "latest", "rdf.yaml")

a_parsed, tf_parsed, pt_parsed = list(map(lambda x: models.parse_model(x), [an_rdf, tf_rdf, pt_rdf]))

# test weight info
w1, w2, w3 = list(map(lambda x: models.get_weight_info(x), [a_parsed, tf_parsed, pt_parsed]))
assert w1 == [] and w2 == ["tensorflow_saved_model_bundle"] and w3 == ["torchscript"]
assert models.get_weight_info(models.parse_model(not_a_model_rdf)) == []

# test get_pprocess_info
assert "" == models.get_pprocess_info(a_parsed, type_="outputs")
assert "zero_mean_unit_variance.ijm" == models.get_pprocess_info(pt_parsed, type_="inputs")
assert "per_sample_scale_range.ijm" == models.get_pprocess_info(tf_parsed, type_="inputs")


# test tensor info
inputs = models.get_tensor_info(models.parse_model(an_rdf), "inputs")
outputs = models.get_tensor_info(parsed_model, "outputs")
assert inputs == {'name': 'raw', 'axes': 'bczyx', 'original-test': 'test_input.npy'}
assert outputs == {'name': 'output', 'axes': 'byxzc', 'original-sample': 'resultImage.tif',
                   'original-test': 'resultImage.npy', 'original-p*process': 'binarize.ijm'}

# test build model
all_model_records = list(map(lambda x: models.build_model(x), collection_test.rdf_paths_3))
assert len(all_model_records) == len(collection_test.rdf_paths_3)

# debug error in yaml parsing (solved)
# for rdf_path in collection_test.rdf_paths_3:
#     print("doing", rdf_path)
#     x = models.build_model(rdf_path)
#     all_model_records.append(x)

a_model_record, pt_model_record, tf_model_record = list(map(lambda x: models.build_model(x), [an_rdf, pt_rdf, tf_rdf]))
pt_model_nopaths = pt_model_record.copy()
pt_model_nopaths.pop("paths")
assert pt_model_nopaths == {'name': 'Neuron Segmentation in EM (Membrane Prediction)',
                            'nickname': 'impartial-shrimp',
                            'rdf-info': {'type': 'model', 'dij-config?': True, 'run-mode': None},
                            'weight-types': ['torchscript'],
                            'inputs': {'name': 'input0',
                                       'axes': 'bczyx',
                                       'original-sample': 'sample_input_0.tif',
                                       'original-test': 'test_input_0.npy',
                                       'original-p*process': 'zero_mean_unit_variance.ijm'},
                            'outputs': {'name': 'output0',
                                        'axes': 'bczyx',
                                        'original-sample': 'sample_output_0.tif',
                                        'original-test': 'test_output_0.npy'}}

parsed_model_nopaths = models.build_model(collection_test.r_c[0])
parsed_model_nopaths.pop("paths")
assert parsed_model_nopaths == {'name': '3D UNet - arabidopsis - ZeroCostDL4Mic', 'nickname': 'humorous-owl',
                                'rdf-info': {'type': 'model', 'dij-config?': True, 'run-mode': None},
                                'weight-types': ['tensorflow_saved_model_bundle'],
                                'inputs': {'name': 'input', 'axes': 'byxzc', 'original-sample': 'exampleImage.tif',
                                           'original-test': 'exampleImage.npy',
                                           'original-p*process': 'per_sample_scale_range.ijm'},
                                'outputs': {'name': 'output', 'axes': 'byxzc', 'original-sample': 'resultImage.tif',
                                            'original-test': 'resultImage.npy', 'original-p*process': 'binarize.ijm'}}

model_no_dij_no_paths = models.build_model(no_dij_config_rdf)
model_no_dij_no_paths.pop("paths")
assert model_no_dij_no_paths == {'name': 'CebraNET Cellular Membranes in Volume SEM', 'nickname': 'joyful-deer',
                                 'rdf-info': {'type': 'model', 'dij-config?': False, 'run-mode': None},
                                 'weight-types': ['torchscript'],
                                 'inputs': {'name': 'input0', 'axes': 'bczyx', 'original-sample': 'sample_input_0.tif',
                                            'original-test': 'test_data_raw.npy', 'original-p*process': 'scale_linear'},
                                 'outputs': {'name': 'output0', 'axes': 'bczyx',
                                             'original-sample': 'sample_output_0.tif',
                                             'original-test': 'test_data_out.npy'}}


model_with_run_mode = models.build_model(Path(ROOTS["collection-root"], "deepimagej", "SkinLesionClassification", "latest", "rdf.yaml"))
assert utils.get_in(model_with_run_mode, ["rdf-info", "run-mode"]) == {'name': 'deepimagej'}

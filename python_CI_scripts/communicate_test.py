from pathlib import Path

import models
from config import ROOTS
import communicate as comm
from models_test import a_model_record, pt_model_record, tf_model_record
from errors_test import models_discriminated

# test paths2string
stringed_model = comm.paths2string(pt_model_record)
assert [str]*5 == list(map(lambda x: type(x), stringed_model["paths"]))

# test serialize_models
out_folder = ROOTS["summa-root"] / "errors_info_test"
out_folder.mkdir(parents=True, exist_ok=True)
comm.serialize_models(list(models_discriminated["error-found"].items())[0], "init", out_folder)

list(map(lambda x: comm.serialize_models(x, "init", out_folder),
         ({"keep-testing": models_discriminated["keep-testing"]} | models_discriminated["error-found"]).items()))

# test format_axes
assert comm.format_axes(a_model_record) == "C,Z,Y,X"
assert comm.format_axes(tf_model_record) == "Y,X,Z,C"

# test get_input_shape
assert comm.get_input_shape(pt_model_record) == '1,32,360,360'
assert comm.get_input_shape(tf_model_record) == '256,256,8,1'

# test get_weight_format

assert comm.get_weight_format(pt_model_record) == 'Pytorch'
assert comm.get_weight_format(tf_model_record) == 'Tensorflow'

affable_sark_model = models.build_model(Path(ROOTS["collection-root"], "10.5281", "zenodo.5764892", "6647674", "rdf.yaml"))
assert comm.get_weight_format(affable_sark_model) == 'Pytorch'

# test get_pprocess

joyful_deer_model = models.build_model(Path(ROOTS["collection-root"], "10.5281", "zenodo.7274275", "8123818", "rdf.yaml"))
assert comm.get_pprocess(joyful_deer_model, "inputs") == "scale_linear.ijm"
assert comm.get_pprocess(joyful_deer_model, "outputs") == "no postprocessing"

creative_panda_model = models.build_model(Path(ROOTS["collection-root"], "10.5281", "zenodo.5817052", "5906839", "rdf.yaml"))
assert comm.get_pprocess(creative_panda_model, "inputs") == "per_sample_scale_range.ijm"
assert comm.get_pprocess(creative_panda_model, "outputs") == "binarize.ijm"

# test build_dij_arg
assert comm.build_dij_arg(tf_model_record) == {'model': 'Cell Segmentation from Membrane Staining for Plant Tissues',
                                               'format': 'Tensorflow',
                                               'preprocessing': 'per_sample_scale_range.ijm',
                                               'postprocessing': 'binarize.ijm',
                                               'axes': 'Y,X,Z,C',
                                               'tile': '256,256,8,1',
                                               'logging': 'Normal'}

assert comm.build_dij_arg(pt_model_record) == {'model': 'Neuron Segmentation in EM (Membrane Prediction)',
                                               'format': 'Pytorch',
                                               'preprocessing': 'zero_mean_unit_variance.ijm',
                                               'postprocessing': 'no postprocessing',
                                               'axes': 'C,Z,Y,X',
                                               'tile': '1,32,360,360',
                                               'logging': 'Normal'}

# test dij_arg_str
# do not format the text, these strings need to be in 1 line to work on DeepImageJ
tf_arg = "model=[Cell Segmentation from Membrane Staining for Plant Tissues] format=Tensorflow preprocessing=[per_sample_scale_range.ijm] postprocessing=[binarize.ijm] axes=Y,X,Z,C tile=256,256,8,1 logging=Normal"

pt_arg = "model=[Neuron Segmentation in EM (Membrane Prediction)] format=Pytorch preprocessing=[zero_mean_unit_variance.ijm] postprocessing=[no postprocessing] axes=C,Z,Y,X tile=1,32,360,360 logging=Normal"

assert comm.dij_arg_str(pt_model_record) == pt_arg
assert comm.dij_arg_str(tf_model_record) == tf_arg

# test build_dij_record
pt_dij_nopath = comm.build_dij_record(pt_model_record)
pt_dij_nopath.pop("model-folder")
assert pt_dij_nopath == {'nickname': 'impartial-shrimp',
                         'name': 'Neuron Segmentation in EM (Membrane Prediction)',
                         'dij-arg': 'model=[Neuron Segmentation in EM (Membrane Prediction)] format=Pytorch preprocessing=[zero_mean_unit_variance.ijm] postprocessing=[no postprocessing] axes=C,Z,Y,X tile=1,32,360,360 logging=Normal',
                         'input-img': 'sample_input_0.tif',
                         'output-img': 'sample_output_0.tif'}

# test write_dij_record
comm.write_dij_record(pt_model_record, verb=True)
comm.write_dij_record(tf_model_record, verb=True)

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

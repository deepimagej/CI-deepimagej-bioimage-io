from config import ROOTS
import communicate as comm
from models_test import a_model_record, pt_model_record, tf_model_record
from errors_test import models_discriminated

b = comm.paths2string(pt_model_record)
assert [str]*5 == list(map(lambda x: type(x), b["paths"]))

# test serialize_models
out_folder = ROOTS["summa-root"] / "errors_info_test"
out_folder.mkdir(parents=True, exist_ok=True)
comm.serialize_models(list(models_discriminated["error-found"].items())[0], "init", out_folder)

list(map(lambda x: comm.serialize_models(x, "init", out_folder),
         ({"keep-testing": models_discriminated["keep-testing"]} | models_discriminated["error-found"]).items()))

# todo: check if it can be read back after serializing
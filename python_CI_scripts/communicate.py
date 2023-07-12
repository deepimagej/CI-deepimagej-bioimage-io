"""Creates files with information for the fiji scripts that will do the testing.
  For dij args: assumes model folders are correctly populated"""

from config import ROOTS, FILES, CONSTANTS
import utils

import json
import yaml


def paths2string(model_record):
    """stringify the paths of the model record"""
    str_model = model_record.copy()
    str_paths = model_record.get("paths").copy()
    for k, v in str_paths.items():
        str_paths[k] = str(v.absolute())
    str_model.update({"paths": str_paths})
    return str_model


def serialize_models(error_data_structure, stage, out_folder=ROOTS["summa-root"]/CONSTANTS["errors-dir-name"]):
    """
    Write the models after they have been discriminated by an error, at a certain stage.
    Model records as json
    Rdf paths as a list in a yaml
    """
    error_key, model_records = error_data_structure
    file_name = stage + "_" + error_key
    str_model_records = list(map(lambda x: paths2string(x), model_records))

    with open(out_folder / (file_name + ".json"), 'w') as fp:
        json.dump({file_name: str_model_records}, fp, indent=2, )

    rdf_paths = list(map(lambda x: utils.get_in(x, ["paths", "rdf-path"]), str_model_records))
    with open(out_folder / (file_name + ".yaml"), "w") as yaml_file:
        yaml.dump(rdf_paths, yaml_file, default_flow_style=False, sort_keys=False)

# TODO dij args
# get the shape with numpy form the test images
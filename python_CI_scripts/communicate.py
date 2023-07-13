"""Creates files with information for the fiji scripts that will do the testing.
  For dij args: assumes model folders are correctly populated"""

from config import ROOTS, FILES, CONSTANTS
import utils

import json
import yaml
import numpy as np


def paths2string(model_record):
    """stringify the paths of the model record"""
    str_model = model_record.copy()
    str_paths = model_record.get("paths").copy()
    for k, v in str_paths.items():
        str_paths[k] = str(v.absolute())
    str_model.update({"paths": str_paths})
    return str_model


def serialize_models(error_data_structure, stage, out_folder=ROOTS["summa-root"] / CONSTANTS["errors-dir-name"]):
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


# TODO (in progress) COMPOSE THE ARGUMENTS FOR DEEPIMAGEJ

def format_axes(model_record):
    """Changes format of axes from rdf (e.g. byzxc) to the one needed for DIJ (e.g. Y,Z,X,C)"""
    axes = utils.get_in(model_record, ["inputs", "axes"])
    axes = axes.replace("b", "").upper()
    return ",".join(list(axes))


def get_input_shape(model_record):
    """
    The tile will be the original shape of the input image (without the initial batch).
    This information comes from the numpy, is stored in the file input_shape.edn
    """
    extracted_path = utils.get_in(model_record, ["paths", "model-dir-path"]) / CONSTANTS["model-dir-name"]
    test_input = np.load(extracted_path / utils.get_in(model_record, ["inputs", "original-test"]))
    return ",".join(list(map(str, test_input.shape[1:])))


def get_weight_format(model_record):
    """Gets the 'dij-macro-compatible' weight format from a model record"""
    # go through valid weight formats,
    # see the first one in model["weigth-types"]
    # associate with correct name e.g. Pytorch not torchscript

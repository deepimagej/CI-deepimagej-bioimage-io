"""Creates files with information for the fiji scripts that will do the testing.
  For dij args: assumes model folders are correctly populated"""

from config import ROOTS, FILES, CONSTANTS
import utils

import json
import yaml
import numpy as np
import os


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
        json.dump({file_name: str_model_records}, fp, indent=2)

    rdf_paths = list(map(lambda x: utils.get_in(x, ["paths", "rdf-path"]), str_model_records))
    with open(out_folder / (file_name + ".yaml"), "w") as yaml_file:
        yaml.dump(rdf_paths, yaml_file, default_flow_style=False, sort_keys=False)


# COMPOSE THE ARGUMENTS FOR DEEPIMAGEJ

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
    try:
        test_input = np.load(extracted_path / utils.get_in(model_record, ["inputs", "original-test"]))
        return ",".join(list(map(str, test_input.shape[1:])))
    # if the model has no numpy images, use the info in the tiffs repo (only happens for fiji/N2v)
    except ValueError as e:
        with open(utils.get_in(model_record, ["paths", "samples-path"]) / "input_shape.edn" ,"r") as f:
            l_ = f.readline()
            l_ = l_.replace(" ", "")
            l_ = l_.replace("[", "")
            l_ = l_.replace("]", "")
        print("Line is", l_[2:])  # debug
        return l_[2:]


def get_weight_format(model_record):
    """Gets the 'dij-macro-compatible' weight format from a model record"""
    # go through valid weight formats,
    # see the first one in model["weigth-types"]
    # Associate key=name of weights in the yaml, and val=name of weights in model record e.g. Pytorch not torchscript

    weight_names = {"torchscript": "Pytorch",
                    "tensorflow_saved_model_bundle": "Tensorflow",
                    "onnx": "Onnx"}

    weights_in_model = model_record.get("weight-types")

    for w in CONSTANTS["valid-weight-keys"]:
        if w in weights_in_model:
            return weight_names[w]


def get_pprocess(model_record, type_="inputs"):
    """Gets the name of the corresponding pre- or post-processing file"""
    pp = {"inputs": "pre", "outputs": "post"}[type_] + "processing"
    name = utils.get_in(model_record, [type_, "original-p*process"], default="")
    extracted_path = utils.get_in(model_record, ["paths", "model-dir-path"]) / CONSTANTS["model-dir-name"]

    if name == "":
        return "no " + pp

    files = list(extracted_path.glob("*" + name + "*"))
    if len(files) == 0:
        return "no " + pp

    return files[0].name


def build_dij_arg(model_record):
    """Builds the argument string needed for the DeepImageJ Run command.
    All args are required and are needed in the right order"""
    return {"model": model_record.get("name"),
            "format": get_weight_format(model_record),
            "preprocessing": get_pprocess(model_record, "inputs"),
            "postprocessing": get_pprocess(model_record, "outputs"),
            "axes": format_axes(model_record),
            "tile": get_input_shape(model_record),
            "logging": "Normal"}


def dij_arg_str(model_record):
    """Makes the DIJ argument as a string"""
    arg_record = build_dij_arg(model_record)
    args = list(map(lambda x: "{}={}".format(x[0], utils.bracketize(x[1])), arg_record.items()))
    return " ".join(args)


def get_model_folder_str(model_record):
    """Gets the model path as a string and with the file separators needed in an imageJ script"""
    str_path = str(utils.get_in(model_record, ["paths", "model-dir-path"])) + os.sep
    return str_path.replace("\\", "/")


def build_dij_record(model_record):
    """Note: the name of the test images will no longer be the one in the yaml,
    because all tiffs generated from numpy have the same name."""
    return {"nickname": model_record.get("nickname"),
            "name": model_record.get("name"),
            "dij-arg": dij_arg_str(model_record),
            "model-folder": get_model_folder_str(model_record),
            "input-img": CONSTANTS["sample-input-name"],
            "output-img": CONSTANTS["sample-output-name"]}


def write_dij_record(model_record, verb=False):
    """Writes the serialized dij record in the model folder"""
    folder = utils.get_in(model_record, ["paths", "model-dir-path"])
    content = build_dij_record(model_record)

    with open(folder / CONSTANTS["dij-args-filename"], 'w') as f:
        json.dump(content, f, indent=2)

    if verb:
        print("written {} in {}".format(CONSTANTS["dij-args-filename"], folder))


"""Define a Model record to hold the relevant data from a parsed rdf"""

from config import ROOTS, CONSTANTS
from pathlib import Path
import utils
import yaml


def parse_model(rdf_path):
    """Takes the path of an rdf.yaml and parses it into a dictionary"""
    with open(rdf_path, "r", encoding="utf-8") as f:
        # encoding needs to be utf-8, otherwise:   UnicodeDecodeError: 'charmap' codec can't decode byte 0x90
        data = yaml.safe_load(f)
    return data


def get_rdf_info(rdf_dict):
    """Gets relevant info from a parsed rdf dictionary"""
    return {"type": rdf_dict["type"],
            "input-images": len(rdf_dict["inputs"]),
            "dij-config?": "deepimagej" in rdf_dict["config"],
            "run-mode": utils.get(rdf_dict, "run_mode")}


def get_paths_info(rdf_path):
    """Gets the different paths the model uses"""
    old_root = ROOTS["collection-root"]
    return {"rdf-path": rdf_path,
            "summa-path": utils.new_root_path(old_root, ROOTS["summa-root"], rdf_path),
            "model-dir-path": utils.new_root_path(old_root, ROOTS["models-root"], rdf_path),
            "samples-path": utils.new_root_path(old_root, ROOTS["samples-root"], rdf_path),
            "manual-samples-path": utils.new_root_path(old_root, ROOTS["samples-root"] / "manual", rdf_path)}


def get_weight_info(rdf_dict):
    """Put relevant weight information in a record, given a parsed rdf.
    The field 'type' is 'None' for unsupported weights"""
    weights_dict = utils.get(rdf_dict, "weights")
    if weights_dict is not None:
        return list(filter(lambda x: x in CONSTANTS["valid-weight-keys"], weights_dict.keys()))
    return []


def get_pprocess_info(rdf_dict, type_="inputs"):
    """Gathers information about pre- or post-processing from the parsed yaml file"""
    pp = "preprocess" if type_ == "inputs" else "postprocess"
    dij_config = utils.get_in(rdf_dict, ["config", "deepimagej", "prediction", pp], default=[None])[0]

    if dij_config is not None:
        name = utils.get_in(dij_config, ["kwargs"], default="")
        return name

    pp = pp + "ing"
    _puts_dict = rdf_dict.get(type_)[0]
    pp_dict = _puts_dict.get(pp, [{}])[0]
    name = utils.get_in(pp_dict, ["name"], default="")
    # mode = utils.get_in(pp_dict, ["kwargs", "mode"], default="")
    return name


def get_tensor_info(rdf_dict, type_="inputs"):
    """Get info about input/output tensors"""
    tensor_list = utils.get(rdf_dict, type_)
    if tensor_list is None:
        return None
    tensor_dict = tensor_list[0]
    tensor_info = {"name": utils.get(tensor_dict, "name"),
                   "axes": utils.get(tensor_dict, "axes")}
    sample_inputs = utils.get(rdf_dict, "sample_" + type_)
    test_inputs = utils.get(rdf_dict, "test_" + type_)

    if sample_inputs is not None:
        tensor_info["original-sample"] = Path(sample_inputs[0]).name
    if test_inputs is not None:
        tensor_info["original-test"] = Path(test_inputs[0]).name

    pp_name = get_pprocess_info(rdf_dict, type_)
    if len(pp_name) > 2:
        tensor_info["original-p*process"] = pp_name

    return tensor_info


def build_model(rdf_path):
    """Generates a Model record, data structure with the information needed for testing a model"""
    rdf_dict = parse_model(rdf_path)
    return {"name": utils.get(rdf_dict, "name"),
            "nickname": utils.get_in(rdf_dict, ["config", "bioimageio", "nickname"]),
            "rdf-info": get_rdf_info(rdf_dict),
            "paths": get_paths_info(rdf_path),
            "weight-types": get_weight_info(rdf_dict),
            "inputs": get_tensor_info(rdf_dict, "inputs"),
            "outputs": get_tensor_info(rdf_dict, "outputs")}

# Note: info about shape and correct sample file will be known only after download

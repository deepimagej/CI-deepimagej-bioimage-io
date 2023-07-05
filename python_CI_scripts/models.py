"""Define a Model record to hold the relevant data from a parsed rdf"""

from config import ROOTS
from pathlib import Path
import utils
import yaml


def parse_model(rdf_path):
    """Takes the path of an rdf.yaml and parses it into a dictionary"""
    with open(rdf_path, "r") as f:
        data = yaml.safe_load(f)
    return data


"""Association of key=name of weights in the yaml, and val=name of weights in model record"""
weight_names = {"torchscript": "Pytorch",
                "tensorflow_saved_model_bundle": "Tensorflow"}


def get_rdf_info(rdf_dict):
    """Gets relevant info from a parsed rdf dictionary"""
    return {"type": rdf_dict["type"],
            "dij-config?": "deepimagej" in rdf_dict["config"],
            "run-mode": utils.get(rdf_dict,"run_mode")}

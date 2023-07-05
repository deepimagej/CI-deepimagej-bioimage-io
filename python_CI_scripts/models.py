"""Define a Model record to hold the relevant data from a parsed rdf"""

from config import ROOTS
from pathlib import Path
import yaml


def parse_model(rdf_path):
    """Takes the path of an rdf.yaml and parses it into a dictionary"""
    with open(rdf_path, "r") as f:
        data = yaml.safe_load(f)
    return data


"""Association of key=name of weights in the yaml, and val=name of weights in model record"""
weight_names = {"torchscript": "Pytorch",
                "tensorflow_saved_model_bundle": "Tensorflow"}

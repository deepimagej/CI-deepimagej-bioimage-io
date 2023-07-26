"""Error keys and messages for the different possible failed test summaries"""

from config import CONSTANTS
import utils

import json
from functools import reduce
from pathlib import Path

"Errors that could happen during the initial checks"
initial_errors = {"no-dij-config": "rdf does not have keys for :config :deepimagej",
                  "no-sample-images": "Sample image tiffs have not been generated from numpy tests files",
                  "no-compatible-weights": "rdf does not have a compatible weight format",
                  "key-run-mode": "Test not yet available: rdf contains the key run_mode with value deepimagej",
                  "not-a-model": "rdf type is not a DL model",  # no test summaries for these though,
                  "incompatible-spec": "Version of the rdf is incompatible with DeepImageJ"}

"Errors that could happen while downloading files for testing"
download_errors = {"download-fail": "Failure to download model with 'bioimageio.core.export_resource_package'",
                   "no-test-images": "No correct sample images to test",
                   "no-pprocess": "P*processing yaml info and files do not agree"}

"Errors that could happen trying to reproduce output with DeepImageJ"
reproduce_errors = {"dij-headless": "Error while running DeepImageJ headless (CI did not produce an output image)",
                    "comparison": "Difference between CI and expected outputs is greater than threshold (CI produced an output image)"}

all_errors = initial_errors | download_errors | reproduce_errors

"Different stages of the CI"
ci_stages = {"initial": "Initial compatibility checks with DeepImageJ",
             "download": "Downloading testing resources for DeepImageJ",
             "reproduce": "Reproduce test outputs with DeepImageJ headless"}


def find_stage(error_key):
    """Gives the stage of an error key. If error key is not found, returns "initial" by default"""
    stages = list(ci_stages.keys())
    if error_key in download_errors:
        return stages[1]
    elif error_key in reproduce_errors:
        return stages[2]
    else:
        return stages[0]


# INITIAL CHECKS
def is_model(model_record):
    """Checks if a model record is from an rdf of type 'model'"""
    return utils.get_in(model_record, ["rdf-info", "type"]) == "model"


def is_no_run_mode(model_record):
    """Checks if the rdf does not have a run_mode key (or if is empty if found)"""
    run_mode_val = utils.get_in(model_record, ["rdf-info", "run-mode"])
    if run_mode_val is None:
        return True
    return utils.get(run_mode_val, "name") != "deepimagej"


def is_any_compatible_weight(model_record):
    """Tells if a model has any compatible weights (checks model record)"""
    w = utils.get(model_record, "weight-types")
    if w is None:
        return False
    return len(w) > 0


"""Association of each possible initial error with a discrimination function.
Order of errors here affects order on how errors are checked"""
init_errors_fns = {"key-run-mode": is_no_run_mode,
                   "no-compatible-weights": is_any_compatible_weight}


# DOWNLOAD CHECKS
def is_success_download(model_record):
    """Checks if model zip downloaded correctly"""
    folder_path = utils.get_in(model_record, ["paths", "model-dir-path"])
    extracted_path = folder_path / CONSTANTS["model-dir-name"]

    return extracted_path.exists()


def is_correct_images(model_record):
    """Checks if correct sample images have been saved"""
    folder_path = utils.get_in(model_record, ["paths", "model-dir-path"])
    input_path = folder_path / CONSTANTS["sample-input-name"]
    output_path = folder_path / CONSTANTS["sample-output-name"]
    return input_path.exists() and output_path.exists()


download_errors_fns = {"download-fail": is_success_download,
                       "no-test-images": is_correct_images}


# REPRODUCE CHECKS
def get_metrics_file(model_record):
    """Returns the file of the output_metrics corresponding to the model record"""
    return Path(utils.get_in(model_record, ["paths", "model-dir-path"]), CONSTANTS["output-metrics-name"])


def get_output_metrics(model_record):
    """Read into a hash-map the output metrics file generated after inference + compare outputs"""
    metrics_path = get_metrics_file(model_record)
    if not metrics_path.exists():
        return {}
    with open(metrics_path, "r") as f:
        metrics_dic = json.load(f)

    return metrics_dic


def is_metrics_produced(model_record):
    """Checks if headless run of deepimagej produced an image (metrics file is an empty map)"""
    return len(get_output_metrics(model_record)) > 0


def is_ok_metrics(model_record):
    metrics_dic = get_output_metrics(model_record)
    return metrics_dic["mse-center"] <= CONSTANTS["mse-threshold"]


reproduce_errors_fns = {"dij-headless": is_metrics_produced,
                        "comparison": is_ok_metrics}


# DISCRIMINATE MODELS BY ERROR TYPE

# Data structure for discriminated models
#   {:keep-testing [list-of-model-records]
#    :error-found  {:error-key1 [list-of-model-records]
#                   :error-key2 [list-of-model-records]}}


def check_error(discriminated_models, error_key_and_fn):
    """Adds results of checking a new error to the data structure for discriminated models.
      To be used as reducing function when iterating over all possible errors"""

    error_key, discriminating_fn = error_key_and_fn
    groups = utils.group_by(discriminating_fn, discriminated_models["keep-testing"])
    to_keep, with_error = utils.get(groups, True), utils.get(groups, False)
    if with_error is None:
        with_error = []
    if to_keep is None:
        to_keep = []
    if "error-found" not in discriminated_models:
        discriminated_models["error-found"] = {}
    discriminated_models["error-found"][error_key] = with_error

    return {"keep-testing": to_keep, "error-found": discriminated_models["error-found"]}


def separate_by_error(models_list, error_fns):
    """Discriminative function should return true to keep testing, false if error occurred
    After an error happens, no more error checks are made for a model"""
    return reduce(check_error, error_fns.items(), {"keep-testing": models_list})

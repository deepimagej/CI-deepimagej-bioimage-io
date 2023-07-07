"""Error keys and messages for the different possible failed test summaries"""

import utils

"Errors that could happen during the initial checks"
initial_errors = {"no-dij-config": "rdf does not have keys for :config :deepimagej",
                  "no-sample-images": "Sample image tiffs have not been generated from numpy tests files",
                  "no-compatible-weights": "rdf does not have a compatible weight format",
                  "key-run-mode": "Test not yet available: rdf contains the key run_mode with value deepimagej",
                  "no-p*process": "Needed p*processing file is not in the attachments (cannot be downloaded)",
                  "not-a-model": "rdf type is not a DL model",  # no test summaries for these though,
                  "incompatible-spec": "Version of the rdf is incompatible with DeepImageJ"}

"Errors that could happen while downloading files for testing"
download_errors = {"download-fail": "failure to download model with 'bioimageio.core.export_resource_package'"}

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

# REPRODUCE CHECKS

# DISCRIMINATE MODELS BY ERROR TYPE

# Data structure for discriminated models
#   {:keep-testing [list-of-model-records]
#    :error-found  {:error-key1 [list-of-model-records]
#                   :error-key2 [list-of-model-records]}}


def check_error(discriminated_models, error_key, discriminating_fn):
    """Adds results of checking a new error to the data structure for discriminated models.
      To be used as reducing function when iterating over all possible errors"""

    groups = utils.group_by(discriminating_fn, discriminated_models["keep-testing"])
    to_keep, with_error = utils.get(groups, True), utils.get(groups, False)
    if "error-found" not in discriminated_models:
        discriminated_models["error-found"] = {}
    discriminated_models["error-found"][error_key] = with_error

    return {"keep-testing": to_keep, "error-found": discriminated_models["error-found"]}

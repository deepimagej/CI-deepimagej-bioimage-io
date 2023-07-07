"""Writing the test summaries"""

import utils
from config import FILES, CONSTANTS
import errors

from bioimageio.core import __version__ as bioimageio_core_version
from bioimageio.spec import __version__ as bioimageio_spec_version
import yaml

default_summa_dict = {"bioimageio_spec_version": bioimageio_spec_version,
                      "bioimageio_core_version": bioimageio_core_version}


def gen_summa_dict(passed=False, error_key="other"):
    d = default_summa_dict.copy()
    if passed:
        d.update({"status": "passed", "name": errors.ci_stages["reproduce"]})
    else:
        stage = errors.find_stage(error_key)
        d.update({"error": errors.all_errors.get(error_key, "Other error"),
                  "status": "failed",
                  "name": errors.ci_stages.get(stage)})
    return d


def write_test_summary(summa_dict, model_record, verb=False):
    """Writes the yaml of the summary-dict in the summary path (in the model.paths)"""
    path = utils.get_in(model_record, ["paths", "summa-path"])
    path.mkdir(parents=True, exist_ok=True)
    file_name = CONSTANTS["summary-name"]
    with open(path / file_name, "w") as yaml_file:
        yaml.dump(summa_dict, yaml_file, default_flow_style=False, sort_keys=False)
    if verb:
        print("written test summary in:", (path / file_name).absolute())


def write_summaries_from_error(error_data_structure, verb=True):
    """Writes the test summaries for the models with errors (entry from a discriminated-models dictionary)"""
    error_key, model_records = error_data_structure
    summa_dict = gen_summa_dict(False, error_key)

    list(map(lambda x: write_test_summary(summa_dict, x), model_records))

    if verb:
        msg = "- Created {:3} test summaries for the error key: {}\n".format(len(model_records), error_key)
        utils.print_and_log(msg, [FILES["summa-readme"]])

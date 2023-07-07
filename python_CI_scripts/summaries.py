import utils
from config import ROOTS, CONSTANTS
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
        yaml.dump(summa_dict, yaml_file, default_flow_style=False)
    if verb:
        print("written test summary in:", path/file_name)

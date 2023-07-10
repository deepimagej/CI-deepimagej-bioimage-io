"""Define the steps for every action: init, download, and reproduce"""
import summaries
from config import FILES, CONSTANTS
import collection
import models
import errors


def initial_pipeline(ini_return, input_json):
    """input_json already parsed"""
    rdf_paths = collection.get_rdfs_to_test(input_json)
    model_records = list(filter(lambda x: errors.is_model(x), (map(lambda x: models.build_model(x), rdf_paths))))
    models_discriminated = errors.separate_by_error(model_records, errors.init_errors_fns)

    # Reset contents of test_summaries/Readme.md
    with open(FILES["summa-readme"], "w") as f:
        f.write(CONSTANTS["summa-readme-header"] + "\n")

    summaries.write_summaries_from_error(models_discriminated["error-found"])

    if ini_return:
        return models_discriminated["keep-testing"]


def download_pipeline(input_json):
    """Downloads files necessary for testing the models"""

    # -> TODO create comm file here for the reproduce step (used to be done in init)
    return


def reproduce_pipeline():
    """Reproduce pipeline for Windows"""
    return

"""Define the steps for every action: init, download, and reproduce"""
import summaries
from config import FILES, CONSTANTS
import utils

import collection
import models
import errors
import downloads


def initial_pipeline(ini_return, input_json):
    """input_json already parsed"""
    rdf_paths = collection.get_rdfs_to_test(input_json)
    model_records = list(filter(lambda x: errors.is_model(x), (map(lambda x: models.build_model(x), rdf_paths))))
    models_discriminated = errors.separate_by_error(model_records, errors.init_errors_fns)
    keep_testing = models_discriminated["keep-testing"]

    # Reset contents of test_summaries/Readme.md
    with open(FILES["summa-readme"], "w") as f:
        f.write(CONSTANTS["summa-readme-header"] + "\n")

    list(map(lambda x: summaries.write_summaries_from_error(x), models_discriminated["error-found"].items()))

    utils.print_and_log("\n{} models to keep testing (after init)\n\n".format(len(keep_testing)),
                        [FILES["summa-readme"]])
    if ini_return:
        return keep_testing


def download_pipeline(input_json):
    """Downloads files necessary for testing the models"""
    keep_testing_ini = initial_pipeline(ini_return=True, input_json=input_json)

    # Reset contents of resources/failed_download_rdfs.txt
    f = open(FILES["failed-downloads"], "w")
    f.close()

    tic = utils.print_start_msg("Started download of {} models".format(len(keep_testing_ini)))
    list(map(lambda x: downloads.download_model(x, verb=True), keep_testing_ini))
    utils.print_elapsed_time(tic, "Finished all downloads")

    models_discriminated = errors.separate_by_error(keep_testing_ini, errors.download_errors_fns)
    keep_testing_dw = models_discriminated["keep-testing"]

    print()
    list(map(lambda x: summaries.write_summaries_from_error(x), models_discriminated["error-found"].items()))

    # todo, get correct images

    utils.print_and_log("\n{} models to keep testing (after download)\n\n".format(len(keep_testing_dw)),
                        [FILES["summa-readme"]])



    # -> TODO create comm file here for the reproduce step: with keep_testing for the reproduce step (used to be done in init)
    return


def reproduce_pipeline():
    """Reproduce pipeline for Windows"""
    return

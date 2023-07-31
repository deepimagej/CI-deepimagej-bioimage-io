"""Define the steps for every action: init, download, and reproduce"""

import config
import summaries
from config import ROOTS, FILES, CONSTANTS
import utils
import collection
import models
import errors
import downloads
import communicate as comm
import run_fiji_scripts

from pathlib import Path
import json


def initial_pipeline(ini_return, input_json):
    """input_json already parsed"""
    rdf_paths = collection.get_rdfs_to_test(input_json)
    all_model_records = list(filter(lambda x: errors.is_model(x), (map(lambda x: models.build_model(x), rdf_paths))))
    manual_models = list(filter(lambda x: errors.is_manually_tested(x), all_model_records))
    model_records = list(filter(lambda x: not errors.is_manually_tested(x), all_model_records))
    models_discriminated = errors.separate_by_error(model_records, errors.init_errors_fns)
    keep_testing = models_discriminated["keep-testing"]

    # Reset contents of test_summaries/Readme.md
    with open(FILES["summa-readme"], "w") as f:
        f.write(CONSTANTS["summa-readme-header"] + "\n")

    utils.print_and_log("\n{} models to test ({} rdf paths)\n\n".format(len(all_model_records), len(rdf_paths)),
                        [FILES["summa-readme"]])

    # Write test summaries for models that were manually tested
    list(map(lambda x: summaries.write_test_summary(summaries.gen_summa_dict(True, manual=True), x), manual_models))

    msg = "- *Created {:3} test summaries for models that passed the CI manually*\n".format(len(manual_models))
    utils.print_and_log(msg, [FILES["summa-readme"]])

    # Write test summaries for errors during init
    list(map(lambda x: summaries.write_summaries_from_error(x), models_discriminated["error-found"].items()))

    # Report the errors & comm files
    # first, delete all existing files in test_summaries/errors_info
    list(map(lambda x: x.unlink(), (ROOTS["summa-root"] / CONSTANTS["errors-dir-name"]).glob("*")))

    list(map(lambda x: comm.serialize_models(x, "all"), ({"models_to_test": all_model_records}).items()))

    list(map(lambda x: comm.serialize_models(x, "init"),
             ({"keep-testing": keep_testing,
               "manually-tested": manual_models} | models_discriminated["error-found"]).items()))

    utils.print_and_log("\n{} models to keep testing after init.\nDetailed information in {}\n\n".format(
        len(keep_testing), ROOTS["summa-root"] / CONSTANTS["errors-dir-name"]), [FILES["summa-readme"]])

    if ini_return:
        return keep_testing


def download_pipeline(input_json):
    """Downloads files necessary for testing the models"""
    keep_testing_ini = initial_pipeline(ini_return=True, input_json=input_json)

    # Reset contents of resources/failed_download_rdfs.txt
    f = open(FILES["failed-downloads"], "w")
    f.close()

    # download
    tic = utils.print_start_msg("Downloading {} models".format(len(keep_testing_ini)))
    list(map(lambda x: downloads.download_model(x[1], verb=True, id_="{:3}/{:3}".format(x[0] + 1, len(keep_testing_ini))),
             enumerate(keep_testing_ini)))
    utils.print_elapsed_time(tic, "Finished all downloads")

    # get correct images
    list(map(lambda x: downloads.save_correct_sample_images(x, verb=False), keep_testing_ini))

    models_discriminated = errors.separate_by_error(keep_testing_ini, errors.download_errors_fns)
    keep_testing_dw = models_discriminated["keep-testing"]
    print()

    # write test summaries for errors during download
    list(map(lambda x: summaries.write_summaries_from_error(x), models_discriminated["error-found"].items()))

    # report the errors & comm files
    list(map(lambda x: comm.serialize_models(x, "download"),
             ({"keep-testing": keep_testing_dw} | models_discriminated["error-found"]).items()))

    utils.print_and_log("\n{} models to keep testing after download.\nDetailed information in {}\n".format(
        len(keep_testing_dw), ROOTS["summa-root"] / CONSTANTS["errors-dir-name"]), [FILES["summa-readme"]])

    # write dij_args.json for every model to test in fiji
    list(map(lambda x: comm.write_dij_record(x), keep_testing_dw))
    utils.print_and_log("\nComm file '{}' created for these {} models \n\n".format(CONSTANTS["dij-args-filename"],
                                                                                   len(keep_testing_dw)),
                        [FILES["summa-readme"]])

    return


def reproduce_pipeline(skip_inference=False):
    """Reproduce pipeline for Windows"""

    # read serialized models to keep testing after download (download_keep-testing.yaml)
    rdf_paths = models.parse_model(ROOTS["summa-root"] / CONSTANTS["errors-dir-name"] / "download_keep-testing.yaml")
    model_records = list((map(lambda x: models.build_model(Path(x)), rdf_paths)))
    model_paths = list(map(lambda x: comm.get_model_folder_str(x), model_records))

    # Serialize config
    config.serialize_config()

    # Call fiji commands
    if not skip_inference:
        run_fiji_scripts.test_models_in_fiji(model_paths)

    # Write test summaries for errors during reproduce
    models_discriminated = errors.separate_by_error(model_records, errors.reproduce_errors_fns)
    models_passing = models_discriminated["keep-testing"]

    list(map(lambda x: summaries.write_summaries_from_error(x), models_discriminated["error-found"].items()))

    # Write test summaries for models that pass
    list(map(lambda x: summaries.write_test_summary(summaries.gen_summa_dict(True), x), models_passing))

    msg = "- *Created {:3} test summaries for models that pass the CI automatically*\n".format(len(models_passing))
    utils.print_and_log(msg, [FILES["summa-readme"]])

    # report the errors & comm files
    list(map(lambda x: comm.serialize_models(x, "reproduce"),
             ({"models-passing": models_passing} | models_discriminated["error-found"]).items()))
    return


def report():
    """
    Report the test information obtained during the CI. Uses:
    - Model info
    - Test summary info
    - Metrics info
    """
    rdf_paths = models.parse_model(ROOTS["summa-root"] / CONSTANTS["errors-dir-name"] / "all_models_to_test.yaml")
    model_records = list((map(lambda x: models.build_model(Path(x)), rdf_paths)))

    models_info = list(
        map(lambda x: summaries.gen_report_record(x[1], "{:3}/{:3}".format(x[0] + 1, len(model_records))),
            enumerate(model_records)))

    with open(FILES["report"], 'w') as fp:
        json.dump({"report": models_info}, fp, indent=2)

    utils.print_and_log("\nReport for the {} models tested written in: {}\n".format(len(model_records),
                                                                                    FILES["report"].absolute()),
                        [FILES["summa-readme"]])

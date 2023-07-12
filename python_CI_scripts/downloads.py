from config import CONSTANTS, FILES
import utils
import errors

from bioimageio.core import export_resource_package as export_zip
import zipfile


def download_model(model_record, verb=False, failed_file=FILES["failed-downloads"]):
    """Downloads a (zip) file from the rdf local path and saves it in a folder with a given name"""

    folder_name = CONSTANTS["model-dir-name"]
    folder_path = utils.get_in(model_record, ["paths", "model-dir-path"])
    folder_path.mkdir(parents=True, exist_ok=True)
    zip_path = folder_path / (folder_name + ".zip")

    if errors.is_success_download(model_record):
        print("  Already downloaded {}".format(folder_path))
        return

    try:
        if verb:
            tic = utils.print_start_msg("Downloading {}".format(folder_path), 1)
        pkg_path = export_zip(utils.get_in(model_record, ["paths", "rdf-path"]),
                              output_path=folder_path / (folder_name + ".zip"),
                              weights_priority_order=CONSTANTS["valid-weight-keys"])
        if verb:
            utils.print_elapsed_time(tic, "Finished download", 1)
    except Exception as e:
        if verb:
            utils.print_elapsed_time(tic, "Download failed", 1)
            utils.print_and_log("{}\n".format(utils.get_in(model_record, ["paths", "rdf-path"])), [failed_file], pr=False)
            # print("Error:", type(e).__name__ + " || " + str(e.args[0]))
            print("  Error:", type(e).__name__ + " || " + str(list(map(lambda x: str(x), e.args))))

    if zip_path.exists():
        with zipfile.ZipFile(zip_path, "r") as zip_file:
            zip_file.extractall(path=folder_path / (CONSTANTS["model-dir-name"]))


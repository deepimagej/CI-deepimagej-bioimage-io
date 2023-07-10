from config import CONSTANTS
import utils

from bioimageio.core import export_resource_package as export_zip
import zipfile


def download_model(model_record, folder_name=CONSTANTS["model-dir-name"], verb=False):
    """Downloads a (zip) file from the rdf local path and saves it in a folder with a given name"""

    folder_path = utils.get_in(model_record, ["paths", "model-dir-path"])
    folder_path.mkdir(parents=True, exist_ok=True)
    zip_path = folder_path / (CONSTANTS["model-dir-name"] + ".zip")
    try:
        pkg_path = export_zip(utils.get_in(model_record, ["paths", "rdf-path"]),
                              output_path=folder_path / (CONSTANTS["model-dir-name"] + ".zip"),
                              weights_priority_order=CONSTANTS["valid-weight-keys"])
        if verb:
            print("Finished download of {}".format(pkg_path))
    except Exception as e:
        if verb:
            print("Download failed {}".format(zip_path))
            print("Error:", type(e).__name__ + "||" + str(e.args))

    if zip_path.exists():
        with zipfile.ZipFile(zip_path, "r") as zip_file:
            zip_file.extractall(path=folder_path / (CONSTANTS["model-dir-name"]))


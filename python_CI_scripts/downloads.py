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
            utils.print_and_log("{}\n".format(utils.get_in(model_record, ["paths", "rdf-path"])), [failed_file],
                                pr=False)
            # print("Error:", type(e).__name__ + " || " + str(e.args[0]))
            print("  Error:", type(e).__name__ + " || " + str(list(map(lambda x: str(x), e.args))))

    if zip_path.exists():
        with zipfile.ZipFile(zip_path, "r") as zip_file:
            zip_file.extractall(path=folder_path / (CONSTANTS["model-dir-name"]))


def save_images(s_path, model_folder_path, type_="input", verb=False):
    """Saves image in the model folder with the default name"""
    dest = model_folder_path / CONSTANTS["sample-" + type_ + "-name"]
    if s_path.exists():
        dest.write_bytes(s_path.read_bytes())
    elif verb:
        print("Error: source path does not exist {}".format(s_path))


def save_correct_sample_images(model_record, verb=False):
    """Get the correct sample images for the model, choose the manual sample tiffs if they exist"""
    samples_path = utils.get_in(model_record, ["paths", "samples-path"])
    model_path = utils.get_in(model_record, ["paths", "model-dir-path"])

    in_order = [samples_path / CONSTANTS["sample-input-name"],
                model_path / CONSTANTS["model-dir-name"] / utils.get_in(model_record, ["inputs", "original-sample"], default=".")]
    in_msgs = ["Using input created from numpy: {}",
               "Using input tiff that comes from the zip: {}"]

    out_order = [utils.get_in(model_record, ["paths", "manual-samples-path"]) / CONSTANTS["CI-output-name"],
                 samples_path / CONSTANTS["sample-output-name"],
                 model_path / CONSTANTS["model-dir-name"] / utils.get_in(model_record, ["outputs", "original-sample"], default=".")]
    out_msgs = ["Using manual output tiff: {}",
                "Using output created from numpy: {}",
                "Using output tiff that comes from the zip: {}"]

    for i, in_path in enumerate(in_order):
        if in_path.exists():
            save_images(in_path, model_path, "input")
            if verb:
                print(in_msgs[i].format(in_path))
            break

    for j, out_path in enumerate(out_order):
        if out_path.exists():
            save_images(out_path, model_path, "output")
            if verb:
                print(out_msgs[j].format(out_path))
            break


"""All the configuration constants in one place"""

from pathlib import Path
import json

"Paths to folder roots"
ROOTS = {"collection-root": Path("..", "bioimageio-gh-pages", "rdfs").absolute(),
         # "summa-root": Path("..", "test_summaries").absolute(),
         "summa-root": Path("test_outputs", "test_summaries").absolute(),  # for debugging
         # "models-root": Path("..", "models").absolute(),
         "models-root": Path("test_outputs", "models").absolute(),  # for debugging
         "samples-root": Path("..", "numpy-tiff-deepimagej").absolute(),
         # "resources-root": Path("..", "resources").absolute(),  # for debugging
         "resources-root": Path("test_outputs", "resources").absolute(),
         "pending-matrix-root": Path("..", "java_CI_scripts", "pending_matrix").absolute(),
         "fiji-home": Path.home() / "blank_fiji"}

"Configuration constants that are files"
FILES = {"config": ROOTS["resources-root"] / "config.json",
         "failed-downloads": ROOTS["resources-root"] / "failed_download_rdfs.txt",
         "fiji-executable": ROOTS["fiji-home"] / "Fiji.app" / "ImageJ-win64.exe",
         "logs": {"out": ROOTS["summa-root"] / "fiji_log_out.txt",
                  "err": ROOTS["summa-root"] / "fiji_log_err.txt"},
         "report": ROOTS["summa-root"] / "report.json",
         "summa-readme": ROOTS["summa-root"] / "Readme.md"}

"Constants that are not files"
CONSTANTS = {"CI-output-name": "CI_OUTPUT.tif",
             "dij-args-filename": "dij_args.json",
             "errors-dir-name": "errors_info",
             "fiji-flags": ["--headless", "--ij2", "--console", "--run"],
             "fiji-scripts-arg-name": "folder",
             "model-dir-name": "the_model",
             "mse-threshold": 9e-2,
             "output-metrics-name": "output_metrics.json",
             "sample-input-name": "sample_input_0.tif",
             "sample-output-name": "sample_output_0.tif",
             "special-headless-chars": [" ", "_", "."],
             "summary-name": "test_summary.yaml",
             "summa-readme-header": "# Report summary",
             "timeout_s": 10*60,
             "valid-weight-keys": ["torchscript", "pytorch_script", "tensorflow_saved_model_bundle", "onnx"]}


def absolutize_nested(dic):
    """absolutize values of dictionary that are files"""
    tuples = map(lambda x: [x[0], absolutize_nested(x[1]) if isinstance(x[1], dict) else str(x[1].absolute())],
                 dic.items())
    return dict(tuples)


def serialize_config(config_file=FILES["config"], verb=True):
    """Serialize config into a json file. Make the files into their absolute path strings"""
    c_dict = {"ROOTS": absolutize_nested(ROOTS),
              "FILES": absolutize_nested(FILES),
              "CONSTANTS": CONSTANTS}

    with open(config_file, 'w') as f:
        json.dump(c_dict, f, indent=2)

    if verb:
        print("Config file saved in {}".format(config_file.absolute()))

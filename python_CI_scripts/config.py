"""All the configuration constants in one place"""

from pathlib import Path

"Paths to folder roots"
ROOTS = {"collection-root": Path("..", "bioimageio-gh-pages", "rdfs").absolute(),
         # "summa-root": Path("..", "test_summaries").absolute(),
         "summa-root": Path("test_outputs", "test_summaries").absolute(),  # for debugging
         # "models-root": Path("..", "models").absolute(),
         "models-root": Path("test_outputs", "models").absolute(),  # for debugging
         "samples-root": Path("..", "numpy-tiff-deepimagej").absolute(),
         "resources-root": Path("..", "resources").absolute(),
         "pending-matrix-root": Path("..", "java_CI_scripts", "pending_matrix").absolute(),
         "fiji-home": Path.home() / "blank_fiji"}

"Configuration constants that are files"
FILES = {"logs": {"out": ROOTS["summa-root"] / "fiji_log_out.txt",
                  "err": ROOTS["summa-root"] / "fiji_log_err.txt"},
         "summa-readme": ROOTS["summa-root"] / "Readme.md"}

"Constants that are not files"
CONSTANTS = {"model-dir-name": "the_model",
             "summary-name": "test_summary.yaml",
             "summa-readme-header": "# Report summary",
             "valid-weight-keys": ["torchscript", "tensorflow_saved_model_bundle", "onnx"]}

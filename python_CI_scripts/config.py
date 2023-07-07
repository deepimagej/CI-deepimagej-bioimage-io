"""All the configuration constants in one place"""

from pathlib import Path

"Paths to folder roots"
ROOTS = {"collection-root": Path("..", "bioimageio-gh-pages", "rdfs").absolute(),
         "summa-root": Path("..", "test_summaries").absolute(),
         "models-root": Path("..", "models").absolute(),
         "samples-root": Path("..", "numpy-tiff-deepimagej").absolute(),
         "resources-root": Path("..", "resources").absolute(),
         "pending-matrix-root": Path("..", "java_CI_scripts", "pending_matrix").absolute()}


"Constants that are not files"
CONSTANTS = {"valid-weight-keys": ["torchscript", "tensorflow_saved_model_bundle", "onnx"],
             "summary-name": "test_summary.yaml"}
"""
Expects LOCAL repository with folders.
    Each folder should have the numpy and tiffs already there.
    This script only writes the shapes in edn format for the CI to consume.
"""
import numpy as np
from pathlib import Path

local_path_str = "../../numpy-tiff-deepimagej/LOCAL/"
local_path = Path(local_path_str)

folders = list(local_path.glob("*"))

file_names = {"input": {"numpy": "test_input", "edn": "input_shape.edn"},
              "output": {"numpy": "test_output", "edn": "output_shape.edn"}}


def get_shapes(folder):
    """
    Generates the lists corresponding to shape the numpy files in the folder
    """
    shapes = {}

    for k in file_names:
        npy = np.load(next(folder.glob(file_names[k]["numpy"]+"*")))
        shapes[k] = list(npy.shape)

    return shapes


def write_edn(folder):
    shapes = get_shapes(folder)
    print("Shapes for model {}".format(folder))
    for k in file_names:
        with open(folder / file_names[k]["edn"], 'w') as f:
            f.write(str(shapes[k]))
        print("Wrote in {}".format(file_names[k]["edn"]))
        print("  {}".format(shapes[k]))


list(map(lambda x: write_edn(x), folders))

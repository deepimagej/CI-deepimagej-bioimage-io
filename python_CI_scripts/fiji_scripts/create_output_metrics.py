# @ String(value="something wrong in args") folder

import datetime
import sys
import json
import os
import shutil
import time
from ij import IJ
from ij.plugin import ImageCalculator


def my_print(s, indent="   *** "):
    """start all prints with a default indent"""
    print(indent + str(s))
    sys.stdout.flush()


my_print("The folder is: " + folder)  # check if the argument is being read correctly
my_print("wd: " + os.getcwd())

# Read config and dij record file
config_dir = "test_outputs/resources/config.json"
with open(config_dir, "r") as f:
    config = json.load(f)
CONSTANTS = config["CONSTANTS"]
ROOTS = config["ROOTS"]

with open(os.path.join(folder, CONSTANTS["dij-args-filename"]), "r") as f:
    DIJ_RECORD = json.load(f)

metrics_file_path = os.path.join(DIJ_RECORD["model-folder"], CONSTANTS["output-metrics-name"])


def write_metrics_file(metrics_dic={}, metrics_file=metrics_file_path):
    """Writes the metrics file (json) with the given metrics in a dictionary"""
    with open(metrics_file, "w") as f:
        json.dump(metrics_dic, f, indent=2)


# open the 2 images that will be compared
tic = time.time()
imp1 = IJ.openImage(DIJ_RECORD["model-folder"] + DIJ_RECORD["output-img"])
imp2 = IJ.openImage(DIJ_RECORD["model-folder"] + CONSTANTS["CI-output-name"])

if imp2 is None:
    write_metrics_file()
    my_print("Run did not produce output")
    my_print("Empty metrics file written in: {}".format(metrics_file_path))
    os._exit(1)


def get5dims(imp):
    """returns the 5 dimensions of an image in a list"""
    width = imp.getWidth()
    height = imp.getHeight()
    channels = imp.getNChannels()
    slices = imp.getNSlices()
    frames = imp.getNFrames()
    return [width, height, channels, slices, frames]


# Sanity check to see if the 2 images (ci output and expected output) have the same dimensions
if get5dims(imp1) != get5dims(imp2):
    write_metrics_file()
    my_print("CI output and expected output do not have the same dimensions")
    my_print("Empty metrics file written in: {}".format(metrics_file_path))
    os._exit(1)

imp3 = ImageCalculator.run(imp1, imp2, "Subtract create 32-bit stack")
imp4 = ImageCalculator.run(imp3, imp3, "Multiply create 32-bit stack")

width, height, channels, slices, frames = get5dims(imp1)
sum_mse = 0.0
sum_mae = 0.0
max_val = -sys.float_info.max
for c in range(0, channels):
    for z in range(0, slices):
        for t in range(0, frames):
            imp4.setPositionWithoutUpdate(c, z, t)
            imp3.setPositionWithoutUpdate(c, z, t)
            imp1.setPositionWithoutUpdate(c, z, t)
            ip4 = imp4.getProcessor()
            ip3 = imp3.getProcessor()
            ip1 = imp1.getProcessor()
            for x in range(0, width):
                for y in range(0, height):
                    sum_mse = sum_mse + ip4.getPixelValue(x, y)
                    sum_mae = sum_mae + abs(ip3.getPixelValue(x, y))
                    val = ip1.getPixelValue(x, y)
                    if val > max_val:
                        max_val = val
mse = sum_mse / (width * height * channels * slices * frames)
mae = sum_mae / (width * height * channels * slices * frames)

# Mae and Mse will be calculated relative to the max intensity value
metrics_dic = {"mse": mse/max_val,
               "mae": mae/max_val,
               "max-val": max_val}

my_print("MSE (relative): {:.5g}".format(metrics_dic["mse"]))
my_print("MAE (relative): {:.5g}".format(metrics_dic["mae"]))
my_print("Max value: {}".format(metrics_dic["max-val"]))

write_metrics_file(metrics_dic)
my_print("Metrics file written in: {}".format(metrics_file_path))
toc = time.time()
my_print("Time taken: {:.1f} min, ({:.0f} s)".format((toc - tic) / 60, toc - tic))

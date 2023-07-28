"""
Fiji script to manually compare 2 images
The first one open should be the results of inference with DeepImageJ
The second, the expected sample output. Provided by the model
"""

import sys
import json
import os
import shutil
import time
from ij import IJ
from ij.plugin import ImageCalculator
from ij import WindowManager as WM

def my_print(s, indent="   *** "):
    """start all prints with a default indent"""
    print(indent + str(s))
    sys.stdout.flush()

# OPEN FIRST IN FIJI THE CI OUTPUT!
l = WM.getIDList()

imp1 = WM.getImage(l[1])
imp2 = WM.getImage(l[0])

print("Image 1 is: " + imp1.getTitle()) # sample output
print("Image 2 is: " + imp2.getTitle())  # CI OUTPUT

if imp2 is None:
    my_print("Run did not produce output")
    os._exit(1)


def get5dims(imp):
    """returns the 5 dimensions of an image in a list"""
    width = imp.getWidth()
    height = imp.getHeight()
    channels = imp.getNChannels()
    slices = imp.getNSlices()
    frames = imp.getNFrames()
    return [width, height, channels, slices, frames]

print("Shape of the images: " + str(get5dims(imp1)))


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

# GET METRICS OF THE CENTER
def get_start_end(size):
    if size <= 3:
        return [0, size]
    return [int(size/4), int(size/2)+1]

my_print("centers of {} are:\n     {}".format(get5dims(imp1), list(map(lambda x: get_start_end(x), get5dims(imp1)))))

sum_mse_center = 0.0
sum_mae_center = 0.0
n_center_pixels = 0
for c in range(*get_start_end(channels)):
    for z in range(*get_start_end(slices)):
        for t in range(*get_start_end(frames)):
            imp4.setPositionWithoutUpdate(c, z, t)
            imp3.setPositionWithoutUpdate(c, z, t)
            imp1.setPositionWithoutUpdate(c, z, t)
            ip4 = imp4.getProcessor()
            ip3 = imp3.getProcessor()
            ip1 = imp1.getProcessor()
            for x in range(*get_start_end(width)):
                for y in range(*get_start_end(height)):
                    sum_mse_center = sum_mse_center + ip4.getPixelValue(x, y)
                    sum_mae_center = sum_mae_center + abs(ip3.getPixelValue(x, y))
                    n_center_pixels += 1

mse_center = sum_mse_center / n_center_pixels
mae_center = sum_mae_center / n_center_pixels

# Mae and Mse will be calculated relative to the max intensity value
metrics_dic = {"mse": mse/max_val,
               "mae": mae/max_val,
               "mse_center": mse_center/max_val,
               "mae_center": mae_center/max_val,
               "max-val": max_val}

my_print("Max value: {}".format(metrics_dic["max-val"]))
my_print("MSE (relative): {:.5g}".format(metrics_dic["mse"]))
my_print("MSE center (relative): {:.5g}".format(metrics_dic["mse_center"]))
my_print("MAE (relative): {:.5g}".format(metrics_dic["mae"]))
my_print("MAE center (relative): {:.5g}".format(metrics_dic["mae_center"]))


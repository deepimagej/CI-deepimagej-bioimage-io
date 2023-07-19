#@ String(value="something wrong in args") folder

import datetime
import sys
import json
import os
import shutil
import time
from ij import IJ


def my_print(s, indent="   *** "):
    """start all prints with a default indent"""
    print(indent + str(s))
    sys.stdout.flush()


my_print("The folder is: " + folder) # check if the argument is being read correctly
my_print("pwd: " + os.getcwd())

config_dir = "test_outputs/resources/config.json"
with open(config_dir, "r") as f:
    config = json.load(f)
CONSTANTS = config["CONSTANTS"]
ROOTS = config["ROOTS"]

with open(os.path.join(folder, CONSTANTS["dij-args-filename"]), "r") as f:
    DIJ_RECORD = json.load(f)


# Functions to move the model_folder to Fiji/models for testing

def copy_model_folder(fiji_home=ROOTS["fiji-home"], model_folder=folder):
    """Copy each file in the model folder to Fiji.app/models"""
    fiji_models = os.path.join(fiji_home, "Fiji.app", "models", CONSTANTS["model-dir-name"])
    shutil.copytree(model_folder + CONSTANTS["model-dir-name"], fiji_models)


def delete_model_folder(fiji_home=ROOTS["fiji-home"]):
    """Delete the model from Fiji.app/models (once inference is finished)"""
    fiji_models = os.path.join(fiji_home, "Fiji.app", "models", CONSTANTS["model-dir-name"])
    try:
        shutil.rmtree(fiji_models)
    except Exception as e:
        # folder does not exist
        return


# Function to test with DIJ from Fiji

def test_one_with_deepimagej(dij_record, fiji_home=ROOTS["fiji-home"]):
    try:
        imp = IJ.openImage(dij_record["model-folder"] + dij_record["input-img"])
    except Exception as e:
        my_print("Error trying to open sample input image")
        return

    if imp is None:
        my_print("Error trying to open sample input image")
        return

    delete_model_folder(fiji_home)
    copy_model_folder(fiji_home, model_folder=dij_record["model-folder"])
    try:
        IJ.run(imp, "DeepImageJ Run", dij_record["dij-arg"])
    except Exception as e:
        my_print("Error during DeepImagej run")
        return
    try:
        ci_output_path = dij_record["model-folder"] + CONSTANTS["CI-output-name"]
        IJ.saveAs("Tiff", ci_output_path)
        my_print("CI output image saved in: " + ci_output_path)
    except Exception as e:
        my_print("Error trying to save output image")

    delete_model_folder(fiji_home)


tic = time.time()
my_print("Started testing model: {}, at {}".format(DIJ_RECORD["nickname"], datetime.datetime.now()))
my_print("Name: {}".format(DIJ_RECORD["name"]))
test_one_with_deepimagej(DIJ_RECORD)
my_print("Finished testing model: {}".format(DIJ_RECORD["nickname"], datetime.datetime.now()))
toc = time.time()
my_print("Time taken: {:.1f} min, ({:.0f} s)".format((toc - tic) / 60, toc - tic))

#@ String(value="something wrong in args") folder

import datetime
import sys
import json
import os
import shutil
from ij import IJ


def my_print(s, indent="   "):
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

my_print(DIJ_RECORD.keys()) # debug
my_print(DIJ_RECORD) # debug


# Aux functions

def print_start_msg(msg, indent_level=0):
    tic = datetime.datetime.now()
    ind = " " * 2 * indent_level
    my_print("{}{}, started at: {}".format(ind, msg, tic))
    return tic


def print_elapsed_time(tic, msg, indent_level=0):
    tac = datetime.datetime.now()
    ind = " " * 2 * indent_level
    my_print("{}{} at: {}".format(ind, msg, tac))
    my_print("{}Elapsed time: {}".format(ind, tac - tic))
    return tac


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
    my_print("input paht:  "+dij_record["model-folder"] + dij_record["input-img"])
    try:
        imp = IJ.openImage(dij_record["model-folder"] + dij_record["input-img"])
    except Exception as e:
        my_print("-- Error trying to open sample input image")
        return

    if imp is None:
        my_print("-- Error trying to open sample input image")
        return

    delete_model_folder()
    copy_model_folder(model_folder=dij_record["model-folder"])
    try:
        IJ.run(imp, "DeepImageJ Run", dij_record["dij-arg"])
    except Exception as e:
        my_print("-- Error during DeepImagej run")
        return
    try:
        IJ.saveAs("Tiff", dij_record["model-folder"] + CONSTANTS["CI-output-name"])
    except Exception as e:
        my_print("-- Error trying to save output image")

    delete_model_folder()


my_print("Hi from script 1")
test_one_with_deepimagej(DIJ_RECORD)


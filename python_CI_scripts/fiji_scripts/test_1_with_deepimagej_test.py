#@ String(value="something wrong in args") folder

import datetime
import sys
import json
import os

print("   The folder is: " + folder) # check if the argument is being read correctly
print("pwd: " + os.getcwd())

config_dir = "test_outputs/resources/config.json"
with open(config_dir, "r") as f:
    config = json.load(f)
CONSTANTS = config["CONSTANTS"]
print(CONSTANTS)


# Aux functions

def print_start_msg(msg, indent_level=0):
    tic = datetime.now()
    indent = " " * 2 * indent_level
    print("{}{}, started at: {}".format(indent, msg, tic))
    return tic


def print_elapsed_time(tic, msg, indent_level=0):
    tac = datetime.now()
    indent = " " * 2 * indent_level
    print("{}{} at: {}".format(indent, msg, tac))
    print("{}Elapsed time: {}".format(indent, tac - tic))
    return tac


print("Hi from script 1")

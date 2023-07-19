#@ String(value="something wrong in args") folder
from ij import IJ
from ij.plugin import ImageCalculator
import os
import sys


def my_print(s, indent="   *** "):
    """start all prints with a default indent"""
    print(indent + str(s))
    sys.stdout.flush()


my_print("The folder is: " + folder) # check if the argument is being read correctly

my_print("Hi from script 2")
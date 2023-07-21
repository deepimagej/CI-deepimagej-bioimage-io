Project directory for the CI scripts in python

# Information
This folder contains the second version of the CI, developed in Python. For more information about the differences,
refer to the [Readme](../java_CI_scripts/Readme.md) of the previous version of the CI.

# Local Setup
You need to perform this 2-step setup in order to **use** or develop this version of the code.

## 1. Clone needed repositories
- change directory to `../bash_scripts/` and run `local_setup.sh` which will clone:
    + the bioimage.io collection repository
    + the numpy-tiffs repository

## 2. Set up the conda enviroment
The required packages for a working conda environment are in `conda_env_packages.yaml`.
It contains the needed dependencies and their versions. You need to have conda or Anaconda previously installed.

Create the environment with:
````
conda env create -f conda_env_packages.yaml
````

# User instructions

## Run usage (-h flag)

````
python -u main.py -h
````

The output should be something like this

````
usage: main.py [-h] [-j JSON_FILE] [-s JSON_STRING] [-i SKIP_INFERENCE] {init,download,reproduce,report}

Python CI for testing bioimagio models in deepimagej

positional arguments:
  {init,download,reproduce,report}
                        # init (DEFAULT) Initial checks & Parse the input and prepares the models to be tested.
                        # download Populate model folders (download files). Build args for DeepImagej headless. 
                        # reproduce Run the models on Fiji with DeepImageJ headless. Compare with the expected output image.
                        # report Generates a report with the results of the run.

options:
  -h, --help            show this help message and exit
  -j JSON_FILE, --json-file JSON_FILE
  -s JSON_STRING, --json-string JSON_STRING
  -i SKIP_INFERENCE, --skip-inference SKIP_INFERENCE

more info at: https://github.com/ivan-ea/CI-deepimagej-bioimage-io/blob/master/python_CI_scripts/Readme.md
````

## Run actions
### Run init with an input file
 
````
python -u main.py -j ..\java_CI_scripts\pending_matrix\lightweight_models.json init
````

### Run with a json string as input
Tested on Windows anaconda cmd
````
python -u main.py -s "<a valid json string literal here>"
````
examples:
````
python -u main.py -s "{\"include\": [{\"resource_id\": \"**\",\"version_id\": \"**\"}]}" init
````
````
python -u main.py -s "{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"}, {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}" init
````

### Running download actions on the use cases
````
python -u main.py -j ..\java_CI_scripts\pending_matrix\use_cases.json download
````
Download a single model
````
python -u main.py -s "{\"include\": [{\"resource_id\": \"10.5281/zenodo.5910854\",\"version_id\": \"6539073\"}]}" download
````


### Running reproduce scripts 
Assumes model folders have been populated and the models to test after download are written on `test_summaries/errors_info/download_keep-testing.yaml`.
````
python -u main.py reproduce
````
Use the `-i true` flag to skip inference. Useful if want to check a previous long run.

### Generating the report
Use this after the `reproduce` step
````
python -u main.py report
````

# Developer information
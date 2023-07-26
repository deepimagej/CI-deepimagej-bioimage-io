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

# Contribute (Developer information)
Detailed explanation on all steps needed to develop the CI including the CI scripts, the CI pipeline and the CI environment.

## CI Python Plan
### 0. Environment Setup
1. Setup Github Actions
  Setup of Conda and python. Get connection with the `core-bioimage-io-python` repository. Available [here](https://github.com/bioimage-io/core-bioimage-io-python). 

2. Setup the Command Line Interface (CLI)

### 1. Init
1. Get paths of models rdf to test from `collection-bioimage-io` repository. Available [here](https://github.com/bioimage-io/collection-bioimage-io).
  Generate json file to decide which model to test. ***~Reuse***

> Future work: Generate json file automatically taking *last version* of all models available in the BioImage Model Zoo.

2. Collect model folders path and generate the folder structure.
  ```
  # FOLDER STRUCTURE
  rdf (renamed to - models/)
  | - 10.5281
  |	| - model_1
  |	| - model_2
  |	| ...
  |	| - model_n
  |		| - rdf.yaml
  |		| - test_summary.yaml
  | - deepimagej
    | - models not working!
  ```
3. Repeat *1.2* to generate again the folder structure but for the test summaries. 
4. Generate model record from `rdf.yaml`file. 
5. Generate test summaries to check everything is working.

### 2. Download
1. Populate model folders from the BioImage Model Zoo.
  1. Download and create each folder.
    1. Weights and processing scripts from `core-bioimage-io-python` repository. Available [here](https://github.com/bioimage-io/core-bioimage-io-python). 
    2. Sample input/output data. 
      * Store in a folder called `model_n/` for each of the models.
      * Collect from the `numpy-tiff-deepimagej` repository. Available [here](https://github.com/ivan-ea/numpy-tiff-deepimagej). ***~Reuse***

2. Generate DeepImageJ Arguments file for each model.
  Grab information from the `rdf.yaml` file and generate an argument for each model. 
  > Not al lyaml files have the same structure.

3. Generate comm file to give testing information to the Fiji Script (3.X).
  `absolute_path_to_model_folder, ../models/rdf.yaml` 

### 3. Reproduce
1. Generate Fiji Commands (one command per model).
2. Generate Fiji Scripts
  1. Setup to run model in DeepImageJ
  2. Run model in DeepImagej and compare output with expected output (*sample output vs. CI output*) ***~Reuse***
3. Generate test summaries to check everything is working.
Project directory for the CI scripts in python

# How to use it

## Run usage (-h flag)

````
python -u main.py -h
````

The output should be something like this

````
usage: main.py [-h] [-j JSON_FILE] [-s JSON_STRING] {init,download,reproduce}

Python CI for testing bioimagio models in deepimagej

positional arguments:
  {init,download,reproduce}
                        # init (DEFAULT) Initial checks & generate folder structures and files for the compatible
                        models to test. # download Populate model folders (download files). Build args for DeepImagej
                        headless. # reproduce Run the models on Fiji with DeepImageJ headless. Create tests summaries.

optional arguments:
  -h, --help            show this help message and exit
  -j JSON_FILE, --json-file JSON_FILE
  -s JSON_STRING, --json-string JSON_STRING

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
TODO
````

### Running reproduce scripts 
Assumes model folders have been populated and their paths are written on `models_to_test.txt`.
````
TODO
````

# Contribute
Detailed explanation on all steps needed to develope the CI including the CI scripts, the CI pipeline and the CI environment.

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

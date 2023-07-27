Project directory for the clojure CI scripts.

# Information

This folder contains the first version of the CI, which was developed in Clojure, and was finished in February 2023.
A new version of the CI, in python, was developed in July 2023. It is in this same repo, in the folder `../python_CI_scripts/`

The code structure is very close in both versions. Although the new one contains improvements,
so it should be the one preferred.

Nevertheless, some functionalities remain only available through the clj version e.g.
- update the collection automatically `pending_matrix/only_collection.json`
- set up fiji and deepimagej in different OS's

# Local Setup
You need to perform this 2-step setup in order to **use** or develop this version of the code.

## 1. Clone needed repositories
- change directory to `../bash_scripts/` and run `local_setup.sh` which will clone:
    + the bioimage.io collection repository
    + the numpy-tiffs repository

## 2. Install Babashka
To run the scripts locally you need babashka installed. It is a standalone program (a 20Mb download) compatible with Linux, Mac, and Windows.
Follow the [installation instructions](https://github.com/babashka/babashka#installation).

# User instructions

## Run usage (-h flag)
````
bb -m core.main -h
````
The output should be something like this:
````

DeepImageJ CI for models from the BioImage Model Zoo (https://bioimage.io/)

Usage: bb -m core.main [action] [options] (in the java_CI_scripts working directory)

Actions:
 init (DEFAULT) Initial checks & generate folder structures and files for the compatible models to test.
 download       Populate model folders (download files). Build args for DeepImagej headless.
 reproduce      Run the models on Fiji with DeepImageJ headless. Create tests summaries.

Options:
  -h, --help                                                 Show help
  -j, --json-file FILE      ./pending_matrix/use_cases.json  Read input from json FILE
  -s, --json-string STRING                                   Read input from raw json STRING
  -u, --unit-test           false                            Run all unit tests

Please refer to the docs page for more information:
        https://github.com/ivan-ea/CI-deepimagej-bioimage-io/blob/master/java_CI_scripts/Readme.md
````

## Run actions

### Run init with an input file
 
````
bb -m core.main init -j pending_matrix/use_cases.json
````

### Run with a json string as input
````
bb -m core.main -s '<a valid json string literal here>'
````
example
````
bb -m core.main -s '{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"}, {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}'
````

### Running download actions on the use cases
````
bb -m core.main download -j pending_matrix/use_cases.json
````

### Running reproduce scripts 
Assumes model folders have been populated and their paths are written on `models_to_test.txt`.
````
bb -m core.main reproduce
````


### Run all unit tests
````
bb -m core.main -u
````

## Run source code directly (functionality that is still not actions)

### Update the models in the pending matrix `only_colection.json`
````
bb "(require 'collection) (collection/generate-pending-matrix-from-collection true)"
````

### Run unit tests for a subset of namespaces
````
bb "(require 'collection-test 'core.cli-test) (clojure.test/run-tests 'collection-test 'core.cli-test)"
````

### Generate the bash script to run the CI
````
bb "(require '[reproduce.run-fiji-scripts :as r]) (r/build-bash-script)"
````
### Setup Fiji and DeepImageJ in default directory `~/blank_fiji/`
````
bb "(require '[reproduce.setup-fiji :as r]) (r/setup-fiji-&-deepimagej3)"
````
### Run script for running models *sequentially*
````
bb "(require '[reproduce.run-fiji-scripts :as r]) (r/grant-exec-permission) (r/run-seq-fiji-script)"
````

## Run as a clojure project
Simply change the beginning in all previous commands: `bb -m core.main` for `clj -M -m core.main`.

Example:
````
clj -M -m core.main -h
````
- This will use the dependencies from `deps.edn` instead of the ones that come with babashka.
- Should give exactly the same results as running with babashka.
- You will need to have clojure installed, more information in the next section.

# Developer information

## CI Plan Diagram
Conceptual organization of the code

![CI_plan](../resources/ci_plan_diagram/CI_plan.png)

## Develop the project on IntelliJ
### 0. Install Java
- Install Java (recommended java 8)
- Install Clojure ([instructions](https://clojure.org/guides/install_clojure))
### 1. Install IntelliJ
### 2. Install Plugins
- Cursive
- Rainbow Brackets (recommended)
- IdeaVim (optional)
### 3. Set up the project
- Open the java_CI_scripts folder as a project (File > Open)
- After that, set up to work with babashka [instructions](https://cursive-ide.com/userguide/babashka.html).
- Go to File > Project Structure
    - go to > Project
        - select SDK java 8
        - select outputs folder to \java_CI_scripts\out
    - go to > Modules
        - add [+] babashka module
        - set the directory of the babashka binary
- To avoid "symbol not resolved" warnings: right-click on each file and mark them as Babashka scripts
### 4. Set up the repl
- Go to Run > Edit Configurations
- [+] add new configuration
- give it a name
- Press play (top right)

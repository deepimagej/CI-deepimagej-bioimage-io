Project directory for the CI scripts.

# Run usage (-h flag)
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

# Run actions

## Run init with an input file
 
````
bb -m core.main init -j pending_matrix/use_cases.json
````

## Run with a json string as input
````
bb -m core.main -s '<a valid json string literal here>'
````
example
````
bb -m core.main -s '{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"}, {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}'
````

## Running download actions on the use cases
````
bb -m core.main download -j pending_matrix/use_cases.json
````

## Running reproduce scripts 
Assumes model folders have been populated and their paths are written on `models_to_test.txt`.
````
bb -m core.main reproduce
````


## Run all unit tests
````
bb -m core.main -u
````

# Run source code directly (functionality that is still not actions)

## Update the models in the pending matrix `only_colection.json`
````
bb "(require 'collection) (collection/generate-pending-matrix-from-collection true)"
````

## Run unit tests for a subset of namespaces
````
bb "(require 'collection-test 'core.cli-test) (clojure.test/run-tests 'collection-test 'core.cli-test)"
````

## Generate bash script to run the CI
````
bb "(require '[reproduce.run-fiji-scripts :as r]) (r/build-bash-script)"
````

# Run as a clojure project
````
clj -M -m core.main -h
````
- This will use the dependencies from `deps.edn` instead of the ones that come with babashka.
- Should give exactly the same results as running with babashka.

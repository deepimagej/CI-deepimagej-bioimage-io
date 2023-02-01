Project directory for the CI scripts.

# Run usage (-h flag)
````
bb -m core.main -h
````
The output should be something like this:
````
DeepImageJ CI for models from the BioImage Model Zoo (https://bioimage.io/)

Usage: bb -m core.main [options] [action] (in the java_CI_scripts working directory)

Options:
  -u, --unit-test           false                            Run all unit tests
  -j, --json-file FILE      ./pending_matrix/use_cases.json  Read input from json FILE
  -s, --json-string STRING                                   Read input from raw json STRING
  -h, --help                                                 Show help

Actions:
 init (DEFAULT) Initial checks & generate folder structures and files for the compatible models to test.
 download       Populate model folders (download files). Build args for DeepImagej headless.
 reproduce      Run the models on Fiji with DeepImageJ headless. Create tests summaries (to-do).
````

# Run actions

## Run init with an input file
 
````
bb -m core.main -j pending_matrix/use_cases.json init
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
bb -m core.main -j pending_matrix/use_cases.json download
````

## Running reproduce scripts on the use cases
Assumes models have been populated and models_to_test.txt created
````
bb -m core.main reproduce
````


## Run all unit tests
````
bb -m core.main -u
````

### Run unit tests for a subset of namespaces
````
bb "(require 'collection-test 'core.cli-test) (clojure.test/run-tests 'collection-test 'core.cli-test)"
````

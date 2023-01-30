Project directory for the CI scripts.

# Info
- [Bioimageio Wiki](https://github.com/bioimage-io/bioimage.io/wiki/Contribute-community-partner-specific-test-summaries)
- [Issue](https://github.com/bioimage-io/collection-bioimage-io/issues/515)

# Run usage (-h flag for)
````
bb -m core.main -h
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

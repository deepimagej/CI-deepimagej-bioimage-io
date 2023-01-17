Project directory for the CI scripts.

# Info
- [Bioimageio Wiki](https://github.com/bioimage-io/bioimage.io/wiki/Contribute-community-partner-specific-test-summaries)
- [Issue](https://github.com/bioimage-io/collection-bioimage-io/issues/515)

## Run main functionality (-h flag for usage)
````
bb -m core.main -h
````

## Run with the default json input file (./pending_matrix/two_models.json)
````
bb -m core.main
````
## Run with other input file
 
````
bb -m core.main -j pending_matrix/use_cases.json
````

## Run with a json string as input
````
bb -m core.main -s '<a valid json string literal here>'
````
example
````
bb -m core.main -s '{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"}, {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}'
````

## Run all unit tests
````
bb -m core.main -u
````

### Run unit tests for a subset of namespaces
````
bb "(require 'collection-test 'core.cli-test) (clojure.test/run-tests 'collection-test 'core.cli-test)"
````

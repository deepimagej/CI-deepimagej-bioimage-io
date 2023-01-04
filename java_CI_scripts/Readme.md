Project directory for the CI scripts.

# Info
- [Bioimageio Wiki](https://github.com/bioimage-io/bioimage.io/wiki/Contribute-community-partner-specific-test-summaries)
- [Issue](https://github.com/bioimage-io/collection-bioimage-io/issues/515)

## Run main functionality (-h flag for usage)
````
bb -m core.main -h
````

## Run all unit tests
````
bb -m core.main -u
````

## Run over the default json input file (./pending_matrix/two_models.json)
````
bb -m core.main
````
or 
````
bb -m core.main -j pending_matrix/two_models.json
````

## Run with a json string as input
````
bb -m core.main -s '<a valid json string literal here>'
````
example
````
bb -m core.main -s '{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"}, {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}'
````


## Run tests of several namespaces
````
 bb "(require '[core.cli-test]) (clojure.test/run-tests 'core.cli-test)"
````

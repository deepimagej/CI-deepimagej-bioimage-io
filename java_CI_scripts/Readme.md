Project directory for the CI scripts.

## Run main functionality (-h flag for info)
````
bb -m core -h
````

## Run all unit tests
````
bb -m core -u
````

## Run over the default json input file (./pending_matrix/two_models.json)
````
bb -m core
````
or 
````
bb -m core -j pending_matrix/two_models.json
````

## Run with a json string as input
````
bb -m core -s '<a valid json string literal here>'
````
example
````
bb -m core -s '{\"include\": [{\"resource_id\": \"10.5281/zenodo.7261974\",\"version_id\": \"7261975\"}, {\"resource_id\": \"deepimagej\",\"version_id\": \"DeepSTORMZeroCostDL4Mic/latest\"}]}'
````


## Run tests of several namespaces
````
 bb "(require '[collection-test]) (clojure.test/run-tests 'collection-test)"
````

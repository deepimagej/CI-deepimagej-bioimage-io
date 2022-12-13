Project directory for the CI scripts.

## Run main of a namespace
` bb -m collection 1 2 "a" "3"`

## Run tests of a namespace
````
 bb "(require '[collection-test]) (clojure.test/run-tests 'collection-test)"
````
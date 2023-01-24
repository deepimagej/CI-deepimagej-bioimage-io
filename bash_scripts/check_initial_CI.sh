#! /usr/bin/env sh

tree -h --du --dirsfirst -L 3 test_summaries/ # test-summaries folder should exist
tree -h --du --dirsfirst -L 5 models/ # models folder should exist and be populated
echo -- Print contents of test summaries
find test_summaries/ -name '*.yaml' -exec head -n 1 {} +
echo -- Print contents of comm file
head -n 30 ~/models_to_test.edn 

Different json files with models for testing

## all_models.json
- Contains all models of the bioimage.io collection.
- Specified via globbing
- Robust against addition/deletion of models
- A lot of models, so not good for small tests

## variety_models.json
Contains models of different characteristics, to see different behaviors during testing.

### A model incompatible with DeepImageJ
- Weights are `pytorch_state_dict`
- "10.5281" "zenodo.6334881" "6346477"

### A tensorflow model
- Weights are `tensorflow_saved_model_bundle`
- "10.5281" "zenodo.5749843" "5888237"
- Has pre- and post-processing

### A pytorch model
- Weights are `torchscript`
- "10.5281" "zenodo.5874741" "5874742"
- Has pre-processing

## two_models.json
- Two random models from the collection
- 10.5281/zenodo.7261974/7261975
- deepimagej/DeepSTORMZeroCostDL4Mic/latest

## two_versions.json
- All two versions of a model
- Obtained via globbing or the "version" field
- 10.5281/zenodo.5749843/**

## only_collection.json
- pending matrix generated from the latest version of the models from [collection.json](https://github.com/bioimage-io/collection-bioimage-io/blob/gh-pages/collection.json)
- These are the models that appear in the bioimage.io website

## use_cases.json
- models from the use-cases that appear in the pre-print

## different_engines.json
Models with all the new compatible weights to the model runner.
- pytorch (wild-whale)
- tf1 (chatty-frog)
- tf2 (placid-llama)
- onnx (nice-peacock)

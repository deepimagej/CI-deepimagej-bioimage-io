# CI-deepimagej-bioimage-io
Scripts and gh workflows for testing the models of the bioimage model zoo in DeepImageJ
## Resources
- [Bioimageio Wiki](https://github.com/bioimage-io/bioimage.io/wiki/Contribute-community-partner-specific-test-summaries)
- [Issue](https://github.com/bioimage-io/collection-bioimage-io/issues/515)


# Local testing of models from the zoo
- Change directory to `python_CI_scripts/` or to `java_CI_scripts/`.
- Follow the instructions from the `Readme` there.

# Information
The code implements a system for the automatic testing of models from the biomage model zoo in deepimagej.

- The input is the specification of the models to test (in a json format)
- The output is a test summary for each of the input models.
  - The test summary "pass" if the model was able to run on DeepImageJ headless mode (and produce the correct output).
  - Otherwise, the test summary "failed" and contains information of the error encountered.

The test summaries are saved in the `gh-pages` branch of this repository,
a summary of the results is generated in the Readme of that branch.

Additional information is also saved in this branch, e.g. a report with the results for every model in `report.json`

## Why?
The content of the test summaries are rendered in each of the model cards of [bioimage.io](https://bioimage.io/).
This will give information to users on whether each of the models will work on DeepImageJ. In this way, the user can 
choose only the models that we know to be working properly, and avoid surprises when downloading a model that will not work.

Models that pass the CI will display on the webpage similarly to the figure below. In the `Test Summary` section of the model card, clicking in `more details`.
![CI_plan](resources/documentation_imgs/dij_pass.png)

On the other hand, models that fail will display the image below on its model card.
![CI_plan](resources/documentation_imgs/dij_fail.png)

The information about the error also can help in addressing it. As it specifies the point where the testing process failed.





# CI-deepimagej-bioimage-io

## Continuous Integration between DeepImageJ and the BioImage Model Zoo
The CI-deepimagej-bioimage-io project enables continuous integration between DeepImageJ and the BioImage Model Zoo, allowing seamless integration of pre-trained deep learning models from the BioImage Model Zoo into DeepImageJ's framework.

### Table of Contents
- [Introduction](#introduction)
- [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
  * [Usage](#usage)
    + [Model Card](#model-card)
    + [Test Summary](#test-summary)
    + [Local Testing](#local-testing)
- [System Overview](#system-overview)
- [Implementation Details](#implementation-details)
  * [Python CI](#python-ci)
  * [Clorjure CI (Deprecated)](#clorjure-ci--deprecated-)
- [Contribute](#contribute)
- [License](#license)
- [Resources:](#resources-)

## Introduction 
DeepImageJ is an open-source plugin for ImageJ that integrates deep learning models into the ImageJ ecosystem. The BioImage Model Zoo is a repository of pre-trained deep learning models designed for biological image analysis. This project aims to bridge the gap between the two, allowing users to easily utilize models from the BioImage Model Zoo within DeepImageJ's environment.

The CI process ensures that only models known to work properly with DeepImageJ are made available to users, avoiding surprises when downloading models that may not function as expected.

## Getting Started

### Prerequisites

- [DeepImageJ](https://github.com/deepimagej/deepimagej): Make sure you have DeepImageJ installed in your ImageJ/Fiji setup.
- [BioImage Model Zoo](https://github.com/bioimage-io/bioimage-io): You should have access to the BioImage Model Zoo repository.

### Installation

Clone this repository:

```bash
git clone https://github.com/deepimagej/CI-deepimagej-bioimage-io.git
``` 

### Usage

#### Model Card
Models that pass the CI will display on the webpage similarly to the figure below. In the Test Summary section of the model card, clicking on [more details] will reveal additional information about the CI process.

![dij_pass](resources/documentation_imgs/dij_pass.png)

On the other hand, models that fail will display the image below on its model card.
![dij_fail](resources/documentation_imgs/dij_fail.png)

The error information can be helpful, as it specifies the where the testing process failed.

#### Test Summary
The CI process is divided into three stages: init, download, and reproduce. Each stage tests the models for specific criteria, and the corresponding errors are detected and reported in the test summary.
CI Stages:
* **init**: The init stage parses the input and prepares the models to be tested.
* **download**: The download stage involves the download of the models from the bioimage.io repository.
* **reproduce**: The reproduce stage runs the models inside Fiji, with DeepImageJ in headless mode. It then compares the output images with the expected output to validate the model's compatibility with DeepImageJ.

#### Local Testing
- Change directory to `python_CI_scripts/` or to `java_CI_scripts/` to your own paths.
- Follow the instructions from the `Readme.md` there.

## System Overview
The CI system operates as follows:
* The input of the system is the specification of the models to test, provided in JSON format.
* The output is a test summary for each of the input models, saved as YAML files.
* The test summary is marked as "pass" if the model was able to run on DeepImageJ headless mode and produce the correct output.
* If the model fails the testing process, the test summary contains information about the encountered error.
* All test summaries are saved in the gh-pages branch of this repository.
* Additional information, such as a detailed report with the results for every model (report.json) and a summary of the results (Readme.md), are also saved in that branch.

![ci_stages](resources/documentation_imgs/ci_concept.png)


## Implementation Details

### Python CI
The CI is implemented in Python and resides in the python folder of this GitHub repository. Detailed information on the Python CI process can be found in the corresponding Python CI README.

### Clorjure CI (Deprecated)
The deprecated Clojure CI implementation is located in the clojure folder. It is no longer in use.

## Contribute
Contributions are welcome! If you find any issues or have suggestions for improvements, please feel free to create an issue or submit a pull request. 

## License
This project is licensed under the [BSD 2-Clause License](LICENSE). See the [LICENSE](LICENSE) file for more details.

## Resources:
- [Bioimageio Wiki](https://github.com/bioimage-io/bioimage.io/wiki/Contribute-community-partner-specific-test-summaries)
- [Issue](https://github.com/bioimage-io/collection-bioimage-io/issues/515)



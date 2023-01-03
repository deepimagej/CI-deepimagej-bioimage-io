# Lessons learnt
## outputs for commands of exploratory run 
- Checking out changes the `$GITHUB_WORKSPACE` env variable
````
uname -a
Linux fv-az562-837 5.15.0-1024-azure #30-Ubuntu SMP Wed Nov 16 23:37:59 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux

java -version
openjdk version "11.0.17" 2022-10-18
OpenJDK Runtime Environment Temurin-11.0.17+8 (build 11.0.17+8)
OpenJDK 64-Bit Server VM Temurin-11.0.17+8 (build 11.0.17+8, mixed mode)

pwd
/home/runner/work/CI-deepimagej-bioimage-io/CI-deepimagej-bioimage-io
````

## Running deepimagej headless Needs
- Folder of the model (name of folder does not matter) needs to be in fiji models folder
  put nickname as name
- Name of the model (:name field in yaml)
- Format (Tensorflow or Pytorch?) <- Download (weights)
- Pre-post processing <- Download (macros)
- axes
- tile
- example image.tif <- Download
- expected output <- Download

# Models from the zoo with multiple weight formats
## onnx and pytorch weights
- affable-shark
- straightforward-crocodile

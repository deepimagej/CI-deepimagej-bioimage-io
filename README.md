# CI-deepimagej-bioimage-io
Scripts and gh workflows for testing the models of the bioimage model zoo in DeepImageJ

# Local Setup
## 1. Install Babashka
To run the scripts locally you need babashka installed. It is a standalone program (a 20Mb download) compatible with Linux Mac, and Windows.
Follow the [installation instructions](https://github.com/babashka/babashka#installation).
## 2. Clone needed repositories
- change directory to `bash_scripts`
- run `local_setup.sh`
- this will clone
  - the bioimage.io collection repository
  - the numpy-tiffs repository

# Local testing
- change directory to `java_CI_scripts`
- follow the instructions from the Readme there

# CI Plan Diagram
![CI_plan](resources/ci_plan_diagram/CI_plan.png)

# [optional] Develop the project on IntelliJ
## 0. Install Java 
- Install Java (recommended java 8)
- Install Clojure
## 1. Install IntelliJ
## 2. Install Plugins
- Cursive
- Rainbow Brackets (recommended)
- IdeaVim
## 3. Set up the project
- Open the java_CI_scripts folder as a project (File > Open)
- After that, set up to work with babashka [instructions](https://cursive-ide.com/userguide/babashka.html).
- Go to File > Project Structure
  - go to > Project
    - select SDK java 8
    - select outputs folder to \java_CI_scripts\out
  - go to > Modules
    - add [+] babashka module
    - set the directory of the babashka binary
## 4. Set up the repl
  - Go to Run > Edit Configurations
  - [+] add new configuration
  - give it a name
  - Press play (top right)

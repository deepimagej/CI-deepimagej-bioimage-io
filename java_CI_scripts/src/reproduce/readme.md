
`test_many_with_deepimagej.clj` will be run from fiji.
- It will run DeepImageJ over many models
- It is in this directory for convenient editing and testing with
  IntelliJ/Cursive
- It has only available the libraries that come with Fiji (e.g. clojure 1.8)
- All babashka libraries will not be available
- Imagej libraries will be available

# Run script from fiji (with DeepImageJ installed)

From this directory
````
~/blank_fiji/Fiji.app/ImageJ-linux64 --headless --console --ij2 --run .\test_many_with_deepimagej.clj
````

From fiji directory
````
./ImageJ-linux64 --headless --console --ij2 --run ~/repos/CI-deepimagej-bioimage-io/java_CI_scripts/src/reproduce/test_many_with_deepimagej.clj

````

## on windows
From CI root directory
````
~\blank_fiji\Fiji.app\ImageJ-win64.exe --headless --console --ij2 --run .\src\reproduce\test_many_with_deepimagej.clj
````

From this directory
````
~\blank_fiji\Fiji.app\ImageJ-win64.exe --headless --console --ij2 --run .\test_many_with_deepimagej.clj
````

# create_output_metrics.py

## on windows
````
C:\Users\hestevez\blank_fiji\Fiji.app\ImageJ-win64.exe --headless --ij2 --console --run .\create_output_metrics.py "folder='C:/Users/hestevez/REPOS/CI-deepimagej-bioimage-io/java_CI_scripts/../models/10.5281/zenodo.5749843/5888237'"
````




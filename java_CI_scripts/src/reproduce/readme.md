# Run fiji script

From this directory
````
~/blank_fiji/Fiji.app/ImageJ-linux64 --headless --console --ij2 --run .\test_many_with_deepimagej.clj
````

From fiji directory
````
./ImageJ-linux64 --headless --console --ij2 --run ~/repos/CI-deepimagej-bioimage-io/java_CI_scripts/src/reproduce/test_many_with_deepimagej.clj

````

## on windows
````
 C:\Users\ivana\blank_fiji\Fiji.app\ImageJ-win64.exe --headless --console --ij2 --run .\test_many_with_deepimagej.clj
````

`test_many_with_deepimagej.clj` will be run from fiji.
- It will run DeepImageJ over many models 
- It is in this directory for convenient editing and testing with
  IntelliJ/Cursive
- It has only available the libraries that come with Fiji (e.g clojure 1.8)
- All babashka libraries will not be available
- imagej libraries will be available

`compare_output.clj` will be also run from fiji.

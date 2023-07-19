# Scripts to be run from within fiji
- `test_1_with_deepimagej.py`
- `create_output_metrics.py`

**Important Note**: 
- The python code of these files needs to be compatible with python 2 (jython), which is what fiji is able to run. 
- No external dependencies can be used (easily (?))
- The conda environment is only relevant for the code of the parent folder.
- Pycharm will complain a lot about warnings and errors in the code -> ignore them.

## Info about command line arguments for fiji scripts
- https://imagej.net/scripting/headless
- https://imagej.net/scripting/parameters

## Running the code (on Windows)
From the parent directory (CI root):
````
C:\Users\hestevez\blank_fiji\Fiji.app\ImageJ-win64.exe --headless --console --ij2 --run test_1_with_deepimagej.py folder=43
````
Tested on: cmd, anaconda prompt, and powershell

**Note**: 
- The working directory of the script is the console working directory from where it is launched
- This is also the directory where the `engines` (from JDLL) will be downloaded

## Examples
`test_1_with_deepiamgej.py`
````
C:\Users\hestevez\blank_fiji\Fiji.app\ImageJ-win64.exe --headless --ij2 --console --run C:\Users\hestevez\REPOS\CI-deepimagej-bioimage-io\python_CI_scripts\fiji_scripts\test_1_with_deepimagej.py folder='C:/Users/hestevez/REPOS/CI-deepimagej-bioimage-io/python_CI_scripts/test_outputs/models/10.5281/zenodo.5764892/6647674/'
````
`create_output_metrics.py`
````
C:\Users\hestevez\blank_fiji\Fiji.app\ImageJ-win64.exe --headless --ij2 --console --run C:\Users\hestevez\REPOS\CI-deepimagej-bioimage-io\python_CI_scripts\fiji_scripts\create_output_metrics.py folder='C:/Users/hestevez/REPOS/CI-deepimagej-bioimage-io/python_CI_scripts/test_outputs/models/10.5281/zenodo.5764892/6647674/'
````
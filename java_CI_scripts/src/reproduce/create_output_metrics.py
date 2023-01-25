#@ String folder
from ij import IJ
from ij.plugin import ImageCalculator
import os

model_dir_name = "the_model"
output_name = "CI_OUTPUT.tif"

def gen_path_to(img_name, model_dir_name=model_dir_name):
    return folder + "/" + model_dir_name + "/" + img_name

imp1 = IJ.openImage(gen_path_to("sample_output_0.tif"))
imp2 = IJ.openImage(folder+"/"+output_name)

# debug
# imp1 = IJ.openImage("/home/cia/Documentos/EPFL/numpy-tiff-deepimagej/10.5281/zenodo.5749843/5888237/sample_output_0.tif");
# imp2 = IJ.openImage("/home/cia/Documentos/EPFL/numpy-tiff-deepimagej/10.5281/zenodo.5749843/5888237/sample_input_0.tif");

if imp2 is None:
    os._exit(1)


imp3 = ImageCalculator.run(imp1, imp2, "Subtract create 32-bit stack")
imp4 = ImageCalculator.run(imp3, imp3, "Multiply create 32-bit stack")

width = imp4.getWidth();
height = imp4.getHeight();
channels = imp4.getNChannels();
slices = imp4.getNSlices();
frames = imp4.getNFrames();
sum_mse = 0.0
sum_mae = 0.0
for c in range(0,channels):
	for z in range(0,slices):
		for t in range(0,frames):
			imp4.setPositionWithoutUpdate(c,z,t)
			imp3.setPositionWithoutUpdate(c,z,t)
			ImageProcessor ip4 = imp4.getProcessor()
			ImageProcessor ip3 = imp3.getProcessor()
			for x in range(0,width):
				for y in range(0,height):
					sum_mse = sum_mse + ip4.getPixelValue(x,y)
					sum_mae = sum_mae + abs(ip3.getPixelValue(x,y))
mse = sum_mse / (width*height)
mae = sum_mae / (width*height)

print("The MSE is " + str(mse) + "and the MAE is " + str(mae))

# TODO fix error:
# [ERROR]   File ".\create_output_metrics.py", line 34
#     ImageProcessor ip4 = imp4.getProcessor()
#                   ^
# SyntaxError: no viable alternative at input 'ip4'

# TODO write metrics in a file (in folder)
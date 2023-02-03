#@ String(value="something wrong in args") folder
from ij import IJ
from ij.plugin import ImageCalculator
import os
import sys

print("   The folder is: " + folder) # check if the argument is being read correctly

model_dir_name = "the_model"
ci_output_name = "CI_OUTPUT.tif"

output_file_name = "output_metrics.edn"
output_file_path = folder + "/" + output_file_name
output_file = open(output_file_path, "w")
output_file.write("{\n")

imp1 = IJ.openImage(folder + "/" + model_dir_name + "/" + "sample_output_0.tif")
imp2 = IJ.openImage(folder + "/" + ci_output_name)

# debug in Lucia laptop
# imp1 = IJ.openImage("/home/cia/Documentos/EPFL/numpy-tiff-deepimagej/10.5281/zenodo.5749843/5888237/sample_output_0.tif");
# imp2 = IJ.openImage("/home/cia/Documentos/EPFL/numpy-tiff-deepimagej/10.5281/zenodo.5749843/5888237/sample_input_0.tif");

if (imp2 is None):
    output_file.write("}")
    output_file.close()
    print("   Run did not produce output")
    print("   Empty metrics file written in: {}".format(output_file_path))
    os._exit(1)

def get5dims(imp):
    """returns the 5 dimensions of an image in a list"""
    width = imp.getWidth()
    height = imp.getHeight()
    channels = imp.getNChannels()
    slices = imp.getNSlices()
    frames = imp.getNFrames()
    return [width, height, channels, slices, frames]

# Sanity check to see if the 2 images (ci output and expected output) have the same dimensions
if get5dims(imp1) != get5dims(imp2):
   output_file.write("}")
   output_file.close()
   print("   CI output and expected output do not have the same dimensions")
   print("   Empty metrics file written in: {}".format(output_file_path))
   os._exit(1)

imp3 = ImageCalculator.run(imp1, imp2, "Subtract create 32-bit stack")
imp4 = ImageCalculator.run(imp3, imp3, "Multiply create 32-bit stack")

width, height, channels, slices, frames = get5dims(imp1)
sum_mse = 0.0
sum_mae = 0.0
max_val = -sys.float_info.max
for c in range(0, channels):
	for z in range(0, slices):
		for t in range(0, frames):
			imp4.setPositionWithoutUpdate(c,z,t)
			imp3.setPositionWithoutUpdate(c,z,t)
			imp1.setPositionWithoutUpdate(c,z,t)
			ip4 = imp4.getProcessor()
			ip3 = imp3.getProcessor()
			ip1 = imp1.getProcessor()
			for x in range(0, width):
				for y in range(0, height):
					sum_mse = sum_mse + ip4.getPixelValue(x,y)
					sum_mae = sum_mae + abs(ip3.getPixelValue(x,y))
					val = ip1.getPixelValue(x,y)
					if val > max_val:
					    max_val = val
mse = sum_mse / (width*height)
mae = sum_mae / (width*height)

# Mae and Mse will be calculated relative to the max intensity value
print_msg = "   The MSE (relative) is {}\n   The MAE (relative) is {}\n   The max_val is {}"
print(print_msg.format(mse/max_val, mae/max_val, max_val))

output_file.write(":mse {:.5f} \n".format(mse/max_val))
output_file.write(":mae {:.5f} \n".format(mae/max_val))
output_file.write(":max_val {:.5f}\n".format(max_val))
output_file.write("}")
output_file.close()
print("   Metrics file written in: {}".format(output_file_path))
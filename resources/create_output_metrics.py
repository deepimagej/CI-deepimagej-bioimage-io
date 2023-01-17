from ij import IJ
from ij.plugin import ImageCalculator

imp1 = IJ.openImage("/home/cia/Documentos/EPFL/numpy-tiff-deepimagej/10.5281/zenodo.5749843/5888237/sample_output_0.tif");
imp2 = IJ.openImage("/home/cia/Documentos/EPFL/numpy-tiff-deepimagej/10.5281/zenodo.5749843/5888237/sample_input_0.tif");
imp3 = ImageCalculator.run(imp1, imp2, "Subtract create 32-bit");
imp4 = ImageCalculator.run(imp3, imp3, "Multiply create 32-bit");

width = imp4.getWidth();
height = imp4.getHeight();
sum = 0.0
for x in range(0,width):
	for y in range(0,height):
		sum = sum + imp4.getProcessor().getPixelValue(x,y)
mse = sum / (width*height)
sum = 0.0
for x in range(0,width):
	for y in range(0,height):
		sum = sum + abs(imp3.getProcessor().getPixelValue(x,y))
mae = sum/(width*height)

print("The MSE is " + str(mse) + "and the MAE is " + str(mae))

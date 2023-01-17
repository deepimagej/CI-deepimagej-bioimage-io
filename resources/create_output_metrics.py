from ij import IJ
from ij.plugin import ImageCalculator

imp1 = IJ.openImage("/home/cia/Documentos/EPFL/numpy-tiff-deepimagej/10.5281/zenodo.5749843/5888237/sample_output_0.tif");
imp2 = IJ.openImage("/home/cia/Documentos/EPFL/numpy-tiff-deepimagej/10.5281/zenodo.5749843/5888237/sample_input_0.tif");
imp3 = ImageCalculator.run(imp1, imp2, "Subtract create 32-bit stack");
imp4 = ImageCalculator.run(imp3, imp3, "Multiply create 32-bit stack");

width = imp4.getWidth();
height = imp4.getHeight();
channels = imp4.getNChannels();
slices = imp4.getNSlices();
frames = imp4.getNFrames();
summse = 0.0
summae = 0.0
for c in range(0,channels):
	for z in range(0,slices):
		for t in range(0,frames):
			imp4.setPositionWithoutUpdate(c,z,t)
			imp3.setPositionWithoutUpdate(c,z,t)
			ImageProcessor ip4 = imp4.getProcessor()
			ImageProcessor ip3 = imp3.getProcessor()
			for x in range(0,width):
				for y in range(0,height):
					summse = summse + ip4.getPixelValue(x,y)
					summae = summae + abs(ip3.getPixelValue(x,y))
mse = summse / (width*height)
mae = summae / (width*height)

print("The MSE is " + str(mse) + "and the MAE is " + str(mae))

imp = IJ.openImage("/home/cia/Imágenes/photo_2022-06-28_15-27-46.jpg");
imp = IJ.openImage("/home/cia/Imágenes/photo_2022-06-28_15-23-34.jpg");
imp1 = WindowManager.getImage("photo_2022-06-28_15-27-46.jpg");
imp2 = WindowManager.getImage("photo_2022-06-28_15-23-34.jpg");
imp3 = ImageCalculator.run(imp1, imp2, "Subtract create 32-bit");
imp3.show();
imp1 = WindowManager.getImage("Result of photo_2022-06-28_15-27-46.jpg");
imp2 = WindowManager.getImage("Result of photo_2022-06-28_15-27-46.jpg");
imp4 = ImageCalculator.run(imp1, imp2, "Multiply create 32-bit");
imp4.show();
width = imp3.getWidth();
height = imp3.getHeight();
sum = 0.0
for x in range(0,width):
	for y in range(0,height):
		sum = sum + imp3.getPixelValue(x,y)
mse = sum / (width*height)

imp5 = IJ.run(imp3, "Abs", "");
sum = 0.0
for x in range(0,width):
	for y in range(0,height):
		sum = sum + imp5.getPixelValue(x,y)
mae = sum/(width*height)

print("The MSE is " + str(mse) + "and the MAE is " + str(mae))

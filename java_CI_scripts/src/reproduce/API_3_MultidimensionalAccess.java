/*
* Not supposed to run in this project
* Snippet of code shared by Daniel Sage on Slack
* Traverse all dimensions of a hyperstack with a 5-nested loop
* Proves that without the abstraction of a method, the solution is copy-pasting code...
* */

/*
package reproduce;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
// Permutation on X/Y and Z/T
// Open the image mitosis.tif

public class API_3_MultidimensionalAccess implements PlugIn {
    public void run(String arg) {
        ImagePlus in = IJ.getImage();
        if (in == null)
            return;
        int nx = in.getWidth();
        int ny = in.getHeight();
        int nc = in.getNChannels();
        int nz = in.getNSlices();
        int nt = in.getNFrames();
        int bitdepth = in.getBitDepth();
        IJ.log(“” + nx + ” ” + ny + ” ” + nz + ” ” + nc + ” ” + nt);
        // Permutation on X/Y and Z/T
        ImagePlus out = IJ.createHyperStack(“Out”, ny, nx, nc, nt, nz, bitdepth);
        for (int c = 1; c <= nc; c++) {
            for (int z = 1; z <= nz; z++) {
                for (int t = 1; t <= nt; t++) {
                    in.setPositionWithoutUpdate(c, z, t);
                    out.setPosition(c, t, z);
                    ImageProcessor ipin = in.getProcessor();
                    ImageProcessor ipout = out.getProcessor();
                    IJ.log(” ” + c + ” ” + z + ” ” + t);
                    for (int x = 0; x < nx; x++) {
                        for (int y = 0; y < ny; y++) {
                            double v = ipin.getPixelValue(x, y);
                            ipout.putPixelValue(y, x, v);
                        }
                    }
                }
            }
        }
        out.show();
    }
}
*/

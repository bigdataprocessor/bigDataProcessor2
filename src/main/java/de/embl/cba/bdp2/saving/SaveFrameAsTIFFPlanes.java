package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.plugin.Binner;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.Views;

import java.util.concurrent.atomic.AtomicBoolean;

public class SaveFrameAsTIFFPlanes implements Runnable {

    private final int c;
    private final int t;
    private final int z;
    private final SavingSettings savingSettings;
    private final AtomicBoolean stop;

    public SaveFrameAsTIFFPlanes( int c,
                                  int t,
                                  int z,
                                  SavingSettings savingSettings,
                                  AtomicBoolean stop) {
        this.c = c;
        this.z = z;
        this.t = t;
        this.savingSettings = savingSettings;
        this.stop = stop;
    }

    @Override
    public void run() {

        if (stop.get()) {
            savingSettings.saveVolumes = true;
            return;
        }
        RandomAccessibleInterval imgStack = savingSettings.rai;
        long[] minInterval = new long[]{
                imgStack.min( DimensionOrder.X ),
                imgStack.min( DimensionOrder.Y ),
                z,
                c,
                t};
        long[] maxInterval = new long[]{
                imgStack.max( DimensionOrder.X ),
                imgStack.max( DimensionOrder.Y ),
                z,
                c,
                t};

        RandomAccessibleInterval newRai = Views.interval(imgStack, minInterval, maxInterval);

        @SuppressWarnings("unchecked")
        ImagePlus impCTZ = ImageJFunctions.wrap( newRai, "slice");
        impCTZ.setDimensions(1, 1, 1);

        // Convert
        //
        if (savingSettings.convertTo8Bit) {
            IJ.setMinAndMax(impCTZ, savingSettings.mapTo0, savingSettings.mapTo255);
            IJ.run(impCTZ, "8-bit", "");
        }

        if (savingSettings.convertTo16Bit) {
            IJ.run(impCTZ, "16-bit", "");
        }
        // Bin and save
        //
        String[] binnings = savingSettings.bin.split(";");

        for (String binning : binnings) {

            if (stop.get()) {
                return;
            }

            String newPath = savingSettings.volumesFilePath;

            // Binning
            ImagePlus impBinned = (ImagePlus) impCTZ.clone();

            int[] binningA = Utils.delimitedStringToIntegerArray(binning, ",");

            if (binningA[0] > 1 || binningA[1] > 1 || binningA[2] > 1) {
                Binner binner = new Binner();
                impBinned = binner.shrink(impCTZ, binningA[0], binningA[1], binningA[2], Binner.AVERAGE);
                newPath = savingSettings.volumesFilePath + "--bin-" + binningA[0] + "-" + binningA[1] + "-" + binningA[2];
            }

            FileSaver fileSaver = new FileSaver(impBinned);

            String sC = String.format("%1$02d", c);
            String sT = String.format("%1$05d", t);
            String sZ = String.format("%1$05d", z);
            String pathCTZ;

            if (imgStack.dimension( DimensionOrder.C ) > 1 || imgStack.dimension( DimensionOrder.T ) > 1) {
                pathCTZ = newPath + "--C" + sC + "--T" + sT + "--Z" + sZ + ".tif";
            } else {
                pathCTZ = newPath + "--Z" + sZ + ".tif";
            }
            fileSaver.saveAsTiff(pathCTZ);
        }
    }
}

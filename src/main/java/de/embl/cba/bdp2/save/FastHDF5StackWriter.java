package de.embl.cba.bdp2.save;

/**
 * Adapted from ilastik4ij repository.
 * https://github.com/ilastik/ilastik4ij
 */

import ch.systemsx.cisd.base.mdarray.*;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.HDF5FloatStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.ProgressHelpers;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FastHDF5StackWriter<T extends RealType<T> & NativeType<T>> implements Runnable {
    private final String dataset;
    private final int compressionLevel;
    private final int numFrames;
    private final int numChannels;
    private final int dimZ;
    private final int dimY;
    private final int dimX;
    private final int current_t;
    private int current_c;
    private SavingSettings savingSettings;
    private AtomicInteger counter;
    private final long startTime;
    private final T nativeType;
    private AtomicBoolean stop;

    public FastHDF5StackWriter(String dataset, SavingSettings savingSettings, int t, AtomicInteger counter, long startTime, AtomicBoolean stop) {
        this.nativeType = (T) Util.getTypeFromInterval(savingSettings.rai );
        long[] dims = new long[savingSettings.rai.numDimensions()];
        savingSettings.rai.dimensions(dims);
        this.numFrames = Math.toIntExact(dims[DimensionOrder.T]);
        this.numChannels = Math.toIntExact(dims[DimensionOrder.C]);
        this.dimZ = Math.toIntExact(dims[DimensionOrder.Z]);
        this.dimY = Math.toIntExact(dims[DimensionOrder.Y]);
        this.dimX =  Math.toIntExact(dims[DimensionOrder.X]);
        this.dataset = dataset;
        this.compressionLevel = savingSettings.compressionLevel;
        this.current_t = t;
        this.savingSettings = savingSettings;
        this.counter = counter;
        this.startTime = startTime;
        this.stop = stop;
    }

    @Override
    public void run() {
        final long totalSlices = numFrames * numChannels;
        RandomAccessibleInterval image = savingSettings.rai;
        for (int c = 0; c < this.numChannels; c++) {
            if (stop.get()) {
                Logger.progress("Stopped save thread: ", "" + this.current_t);
                return;
            }
            long[] minInterval = new long[]{
                    image.min( DimensionOrder.X ),
                    image.min( DimensionOrder.Y ),
                    image.min( DimensionOrder.Z ),
                    c,
                    this.current_t};
            long[] maxInterval = new long[]{
                    image.max( DimensionOrder.X ),
                    image.max( DimensionOrder.Y ),
                    image.max( DimensionOrder.Z ),
                    c,
                    this.current_t};
            RandomAccessibleInterval newRai = Views.interval(image, minInterval, maxInterval);
            // Convert
            newRai = SaveImgHelper.convertor(newRai, this.savingSettings);
            Img<T> imgChannelTime;
            imgChannelTime = ImgView.wrap(newRai, new CellImgFactory(this.nativeType));

            // Bin, project and save
            String[] binnings = savingSettings.bin.split(";");

            for (String binning : binnings) {
                if (stop.get()) {
                    Logger.progress("Stopped save thread @ merge: ", "" + current_t);
                    return;
                }
                String newPath = savingSettings.volumesFilePathStump;
                // Binning
                ImgPlus<T> impBinned = new ImgPlus<>(imgChannelTime, "", FileInfos.AXES_ORDER);
                int[] binningA = Utils.delimitedStringToIntegerArray(binning, ",");
                if (binningA[0] > 1 || binningA[1] > 1 || binningA[2] > 1) {
                    newPath = SaveImgHelper.doBinning(impBinned, binningA, newPath, null);
                }
                String sC = String.format("%1$02d", c);
                String sT = String.format("%1$05d", current_t);
                newPath = newPath + "--C" + sC + "--T" + sT + ".h5";

                if (savingSettings.saveVolumes ) {
                    this.current_c = c;
                    writeHDF5(impBinned, newPath);
                }
                // Save projections
                if (savingSettings.saveProjections ) {
                    ImagePlus imagePlusImage = ImageJFunctions.wrap(newRai, "");
                    ProjectionXYZ.saveAsTiffXYZMaxProjection(imagePlusImage, c, this.current_t, newPath);
                }
                counter.incrementAndGet();
            }
            if (!stop.get()) {
                ProgressHelpers.logProgress(totalSlices, counter, startTime, "Saved file ");
            }
        }
    }

    private void writeHDF5(ImgPlus<T> imgBinned, String filename) {
        final long[] dims = {dimZ, dimY, dimX}; //depth,row,col order
        String shape = Arrays.stream(dims)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", "));
        Logger.info(String.format("Exporting image of shape (%s). Axis order: 'XYZCT'", shape));

        try (IHDF5Writer writer = HDF5Factory.open(filename)) {
            if (nativeType instanceof UnsignedByteType) {
                write(imgBinned, writer, dims, UnsignedByteType.class);
            } else if (nativeType instanceof UnsignedShortType) {
                write(imgBinned, writer, dims, UnsignedShortType.class);
            } else if (nativeType instanceof UnsignedIntType) {
                write(imgBinned, writer, dims, UnsignedIntType.class);
            } else if (nativeType instanceof UnsignedLongType) {
                write(imgBinned,writer, dims, UnsignedLongType.class);
            } else if (nativeType instanceof FloatType) {
                write(imgBinned, writer, dims, FloatType.class);
            } else {
                throw new IllegalArgumentException("Unsupported Type: " + nativeType.getClass().toString());
            }
        }
    }

    private void write(ImgPlus<T> image,IHDF5Writer writer, long[] datasetDims, Class pixelClass) {
        int[] blockSize = blockSize(datasetDims);
        createMDArray(writer, datasetDims, blockSize, pixelClass);

        RandomAccess<T> rai = image.randomAccess();
        if (image.dimensionIndex(Axes.TIME) >= 0) {
            rai.setPosition(this.current_t, image.dimensionIndex(Axes.TIME));
        }
        if (image.dimensionIndex(Axes.CHANNEL) >= 0){
            rai.setPosition(this.current_c, image.dimensionIndex(Axes.CHANNEL));
        }
        for (int z = 0; z < dimZ; z++) {
            if (image.dimensionIndex(Axes.Z) >= 0) {
                rai.setPosition(z, image.dimensionIndex(Axes.Z));
            }
            // init MD-array
            Object[] flatArr = new Object[dimY * dimX];
            // copyVolumeRAI data XY slice
            for (int x = 0; x < dimX; x++) {
                rai.setPosition(x, image.dimensionIndex(Axes.X));
                for (int y = 0; y < dimY; y++) {
                    if (stop.get()) {
                        savingSettings.saveProjections = false;
                        Logger.progress("Stopped save thread @ writeHDF5: ", "" + current_t);
                        return;
                    }
                    rai.setPosition(y, image.dimensionIndex(Axes.Y));
                    int index = y * dimX + x;
                    flatArr[index] = getValue(rai, pixelClass);
                }
            }
            long[] offset = {z, 0, 0};
            long[] sliceDims = getXYSliceDims(datasetDims);
            // save data
            writeMDArray(writer, flatArr, offset, sliceDims, pixelClass);
        }
        Logger.info("compressionLevel: " + compressionLevel);
        Logger.info("Finished writing the HDF5_STACKS.");
    }

    private void writeMDArray(IHDF5Writer writer, Object[] flatArr, long[] offset, long[] sliceDims, Class pixelClass) {
        if (pixelClass == UnsignedByteType.class) {
            byte[] arr = new byte[flatArr.length];
            for (int i = 0; i < flatArr.length; i++) {
                arr[i] = (byte) flatArr[i];
            }
            MDByteArray mdArray = new MDByteArray(arr, sliceDims);
            writer.uint8().writeMDArrayBlockWithOffset(dataset, mdArray, offset);
        } else if (pixelClass == UnsignedShortType.class) {
            short[] arr = new short[flatArr.length];
            for (int i = 0; i < flatArr.length; i++) {
                arr[i] = (short) flatArr[i];
            }
            MDShortArray mdArray = new MDShortArray(arr, sliceDims);
            writer.uint16().writeMDArrayBlockWithOffset(dataset, mdArray, offset);
        } else if (pixelClass == UnsignedIntType.class) {
            int[] arr = new int[flatArr.length];
            for (int i = 0; i < flatArr.length; i++) {
                arr[i] = (int) flatArr[i];
            }
            MDIntArray mdArray = new MDIntArray(arr, sliceDims);
            writer.uint32().writeMDArrayBlockWithOffset(dataset, mdArray, offset);
        } else if (pixelClass == UnsignedLongType.class) {
            long[] arr = new long[flatArr.length];
            for (int i = 0; i < flatArr.length; i++) {
                arr[i] = (long) flatArr[i];
            }
            MDLongArray mdArray = new MDLongArray(arr, sliceDims);
            writer.uint64().writeMDArrayBlockWithOffset(dataset, mdArray, offset);
        } else if (pixelClass == FloatType.class) {
            float[] arr = new float[flatArr.length];
            for (int i = 0; i < flatArr.length; i++) {
                arr[i] = (float) flatArr[i];
            }
            MDFloatArray mdArray = new MDFloatArray(arr, sliceDims);
            writer.float32().writeMDArrayBlockWithOffset(dataset, mdArray, offset);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + pixelClass);
        }
    }

    private Object getValue(RandomAccess<T> rai, Class pixelClass) {
        if (pixelClass == UnsignedByteType.class) {
            UnsignedByteType type = (UnsignedByteType) rai.get();
            return Integer.valueOf(type.get()).byteValue();
        } else if (pixelClass == UnsignedShortType.class) {
            UnsignedShortType type = (UnsignedShortType) rai.get();
            return Integer.valueOf(type.get()).shortValue();
        } else if (pixelClass == UnsignedIntType.class) {
            UnsignedIntType type = (UnsignedIntType) rai.get();
            return Long.valueOf(type.get()).intValue();
        } else if (pixelClass == UnsignedLongType.class) {
            UnsignedLongType type = (UnsignedLongType) rai.get();
            return type.get();
        } else if (pixelClass == FloatType.class) {
            FloatType type = (FloatType) rai.get();
            return type.get();
        } else {
            throw new IllegalArgumentException("Unsupported type: " + pixelClass);
        }
    }

    private void createMDArray(IHDF5Writer writer, long[] datasetDims, int[] blockSize, Class pixelClass) {
        if (pixelClass == UnsignedByteType.class) {
            writer.uint8().createMDArray(dataset, datasetDims, blockSize, HDF5IntStorageFeatures.createDeflationDelete(compressionLevel));
        } else if (pixelClass == UnsignedShortType.class) {
            writer.uint16().createMDArray(dataset, datasetDims, blockSize, HDF5IntStorageFeatures.createDeflationDelete(compressionLevel));
        } else if (pixelClass == UnsignedIntType.class) {
            writer.uint32().createMDArray(dataset, datasetDims, blockSize, HDF5IntStorageFeatures.createDeflationDelete(compressionLevel));
        } else if (pixelClass == UnsignedLongType.class) {
            writer.uint64().createMDArray(dataset, datasetDims, blockSize, HDF5IntStorageFeatures.createDeflationDelete(compressionLevel));
        } else if (pixelClass == FloatType.class) {
            writer.float32().createMDArray(dataset, datasetDims, blockSize, HDF5FloatStorageFeatures.createDeflationDelete(compressionLevel));
        } else {
            throw new IllegalArgumentException("Unsupported type: " + pixelClass);
        }
    }

    private int[] blockSize(long[] datasetDims) {
        // expect rank 5 dims with xyzct axis order
        long bSize;
        if (datasetDims[0] > 1) {
            // if z-axis is non-singleton use 32x32x32 chunk size. Change in BLOCK_SIZE_3D if needed.
            bSize = FileInfos.HDF5_BLOCK_SIZE_3D;
        } else {
            // otherwise use 128x128 chunk size
            bSize = FileInfos.HDF5_BLOCK_SIZE_2D;
        }
        int[] result = new int[datasetDims.length];
        for (int i = 0; i < datasetDims.length; i++) {
            result[i] = (int) Math.min(bSize, datasetDims[i]);
        }
        return result;
    }

    private long[] getXYSliceDims(long[] datasetDims) {
        // expect rank 5 dims with xyzct axis order
        long[] result = datasetDims.clone();
        result[0] = 1;
        return result;
    }
}

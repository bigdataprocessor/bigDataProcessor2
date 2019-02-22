package de.embl.cba.bdp2.saving;

import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.imaris.ImarisDataSet;
import de.embl.cba.imaris.ImarisUtils;
import de.embl.cba.imaris.ImarisWriter;
import de.embl.cba.bdp2.logging.IJLazySwingLogger;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.MonitorThreadPoolStatus;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class SaveCentral {
    private static Logger logger = new IJLazySwingLogger();
    public static boolean interruptSavingThreads;

    public static void goSave(SavingSettings savingSettings, ExecutorService es) {
        if (savingSettings.fileType.equals( SavingSettings.FileType.TIFF_as_PLANES)) {
            saveTIFFAsPlanes(savingSettings, es);
        } else if (savingSettings.fileType.equals( SavingSettings.FileType.TIFF_as_STACKS)) {
            saveTIFFAsStacks(savingSettings, es);
        } else if (savingSettings.fileType.equals( SavingSettings.FileType.HDF5_STACKS )) {
            saveHDFStacks(savingSettings, es);
        } else if (savingSettings.fileType.equals( SavingSettings.FileType.HDF5_IMARIS_BDV)) {
            saveIMARIStacks(savingSettings, es);
        }
    }

    private static void saveTIFFAsPlanes(SavingSettings savingSettings, ExecutorService es) {
        List<Future> futures = new ArrayList<>();
        for ( int c = 0; c < savingSettings.image.dimension(FileInfoConstants.C ); c++) {
            for ( int t = 0; t < savingSettings.image.dimension(FileInfoConstants.T ); t++) {
                for ( int z = 0; z < savingSettings.image.dimension(FileInfoConstants.Z ); z++) {
                    futures.add(es.submit(
                            new SaveImgAsTIFFPlanes(c, t, z, savingSettings)
                    ));
                }
            }
        }
        // Monitor the progress
        Thread thread = new Thread(new Runnable() {
            public void run() {
                MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures, "Saved to disk: ", FileInfoConstants.PROGRESS_UPDATE_MILLISECONDS);
            }
        });
        thread.start();
    }

    private static void saveTIFFAsStacks(SavingSettings savingSettings, ExecutorService es) {
        saveTIFFForEachChannelAndTimePoint(savingSettings, es);
    }

    private static void saveHDFStacks(SavingSettings savingSettings, ExecutorService es) {
        saveHDF5ForEachChannelAndTimePoint(savingSettings, es);
    }

    private static void saveIMARIStacks(SavingSettings savingSettings, ExecutorService es) {
        saveIMARISForEachChannelAndTimePoint(savingSettings, es);
    }

    private static void saveTIFFForEachChannelAndTimePoint(SavingSettings savingSettings, ExecutorService es) {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.image.dimension(FileInfoConstants.T );
        for (int t = 0; t < timeFrames; t++) {
            futures.add(
                    es.submit(
                            new SaveImgAsTIFFStacks(t, savingSettings, counter, startTime)
                    ));
        }
        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures, "Saved to disk: ", FileInfoConstants.PROGRESS_UPDATE_MILLISECONDS));
        thread.start();
    }

    private static void saveHDF5ForEachChannelAndTimePoint(SavingSettings savingSettings, ExecutorService es) {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.image.dimension(FileInfoConstants.T );
        NativeType imageType = Util.getTypeFromInterval(savingSettings.image);
        for (int t = 0; t < timeFrames; t++) {
            if (imageType instanceof UnsignedByteType) {
                futures.add(es.submit(
                                new SaveImgAsHDF5Stacks<UnsignedByteType>("Data", savingSettings, t, counter, startTime)
                        ));
            } else if (imageType instanceof UnsignedShortType) {
                futures.add(
                        es.submit(
                                new SaveImgAsHDF5Stacks<UnsignedShortType>("Data", savingSettings, t, counter, startTime)
                        ));
            } else if (imageType instanceof FloatType) {
                futures.add(
                        es.submit(
                                new SaveImgAsHDF5Stacks<FloatType>("Data", savingSettings, t, counter, startTime)
                      ));
            } else {
                // throw Type not found exception : TODO --ashis
            }
        }
        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures, "Saved to disk: ", FileInfoConstants.PROGRESS_UPDATE_MILLISECONDS));
        thread.start();
    }


    private static void saveIMARISForEachChannelAndTimePoint(
            SavingSettings savingSettings,
            ExecutorService es) {
        List<Future> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        ImarisDataSet imarisDataSetProperties = getImarisDataSet(savingSettings);
        final long startTime = System.currentTimeMillis();
        long timeFrames = savingSettings.image.dimension(FileInfoConstants.T );
        NativeType imageType = Util.getTypeFromInterval(savingSettings.image);
        for (int t = 0; t < timeFrames; t++) {
            if (imageType instanceof UnsignedByteType) {
                futures.add(es.submit(
                                new SaveImgAsIMARIS<UnsignedByteType>(savingSettings, imarisDataSetProperties, t, counter, startTime)
                        ));
            } else if (imageType instanceof UnsignedShortType) {
                futures.add(
                        es.submit(
                                new SaveImgAsIMARIS<UnsignedShortType>(
                                        savingSettings,
                                        imarisDataSetProperties,
                                        t,
                                        counter,
                                        startTime)
                        ));
            } else if (imageType instanceof FloatType) {
                futures.add(
                        es.submit(
                                new SaveImgAsIMARIS<FloatType>(savingSettings, imarisDataSetProperties, t, counter, startTime)
                        ));
            }
        }
        // Monitor the progress
        Thread thread = new Thread(() -> MonitorThreadPoolStatus.showProgressAndWaitUntilDone(futures, "Saved to disk: ", FileInfoConstants.PROGRESS_UPDATE_MILLISECONDS));
        thread.start();
    }

    private static ImarisDataSet getImarisDataSet( SavingSettings savingSettings ) {

        ImagePlus image = Utils.wrapToCalibratedImagePlus(
                savingSettings.image,
                savingSettings.voxelSize,
                savingSettings.unit,
                "wrapped");

        String[] binnings = savingSettings.bin.split(";");
        int[] binning = Utils.delimitedStringToIntegerArray(binnings[0], ",");

        ImarisDataSet imarisDataSet = new ImarisDataSet(
                image,
                binning,
                savingSettings.parentDirectory,
                savingSettings.fileBaseNameIMARIS);

        imarisDataSet.setLogger( new de.embl.cba.logging.IJLazySwingLogger() );

        if (SaveCentral.interruptSavingThreads) {
            return null;
        }

        ImarisWriter.writeHeaderFile(
                imarisDataSet,
                savingSettings.parentDirectory,
                savingSettings.fileBaseNameIMARIS + ".ims"
        );


        ArrayList<File> imarisFiles = ImarisUtils.getImarisFiles(savingSettings.parentDirectory);

        if (imarisFiles.size() > 1) {
            ImarisWriter.writeCombinedHeaderFile(imarisFiles, "meta.ims");
        }

        // TODO: remove below
//        ImarisWriter.writeHeaderFile(
//                imarisDataSet, savingSettings.parentDirectory,
//                savingSettings.fileBaseNameIMARIS + ".h5");

        logger.info("Image sizes at different resolutions:");
        Utils.logArrayList( imarisDataSet.getDimensions());

        logger.info("Image chunking:");
        Utils.logArrayList( imarisDataSet.getChunks());

        return imarisDataSet;
    }
}

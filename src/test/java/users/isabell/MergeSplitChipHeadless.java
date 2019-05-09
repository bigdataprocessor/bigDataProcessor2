package users.isabell;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.splitviewmerge.RegionOptimiser;
import de.embl.cba.bdp2.process.splitviewmerge.SplitImageMerger;
import de.embl.cba.bdp2.saving.AbstractImgSaver;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.IOException;
import java.util.ArrayList;


public class MergeSplitChipHeadless
{
    public static < R extends RealType< R > & NativeType< R > > void main( String[] args) throws IOException
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        /**
         * Open Data
         */

        final Image< R > image = bdp.openHdf5Data(
                "/Users/tischer/Desktop/stack_0_channel_0",
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5",
                "Data" );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSpacing( new double[]{0.13, 0.13, 1.04} );

        /**
         * Merge Split
         */

        final ArrayList< long[] > minima = new ArrayList<>();
        minima.add( new long[]{ 22, 643 } );
        minima.add( new long[]{ 896, 46 } );
        final long[] span = { 1000 , 1000 };

        final Image< R > merge = SplitImageMerger.merge( image, minima, span );

        bdp.showImage( merge );

        /**
         * Save as Tiff Stacks
         */

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.nThreads = Runtime.getRuntime().availableProcessors();
        savingSettings.saveVolumes = true;
        savingSettings.volumesFilePath = "/Users/tischer/Desktop/stack_0_channel_0-volumes2/volume";
        savingSettings.saveProjections = true;
        savingSettings.projectionsFilePath = "/Users/tischer/Desktop/stack_0_channel_0-projections2/projection";
        savingSettings.isotropicProjectionResampling = true;
        savingSettings.isotropicProjectionVoxelSize = 0.5;

        final AbstractImgSaver imgSaver = new BigDataProcessor2().saveImage( merge, savingSettings );


    }

}

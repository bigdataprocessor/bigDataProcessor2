package example;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.Binner;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;

import java.io.File;

public class SaveSingleChanneHdf5SeriesAsTiffStacks
{

    public static void main(String[] args)
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_0_channel_0";

        final int numIOThreads = 4; // TODO

        final String loadingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.h5";
        final String dataset = "Data";

        final Image image = bdp.openHdf5Image(
                directory,
                loadingScheme,
                filterPattern,
                dataset );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSpacing( 0.13, 0.13, 1.04 );

        bdp.showImage( image );

        final Image binnedImage = Binner.bin( image, new long[]{ 1, 1, 1, 0, 0 } );
        //   bdp.showImage( bin );


        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.nThreads = 1;
        savingSettings.saveProjections = true;
        savingSettings.volumesFilePath = "/Users/tischer/Desktop/stack_0_channel_0-asTIFF-volumes/im";
        savingSettings.saveVolumes = true;
        savingSettings.projectionsFilePath = "/Users/tischer/Desktop/stack_0_channel_0-asTIFF-projections/im";

        Utils.saveImageAndWaitUntilDone( bdp, savingSettings, binnedImage );

    }

}

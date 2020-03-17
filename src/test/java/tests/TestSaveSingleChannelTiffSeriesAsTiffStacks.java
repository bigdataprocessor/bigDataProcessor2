package tests;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import loci.common.DebugTools;
import org.junit.Test;

import java.io.File;

import static de.embl.cba.bdp2.utils.FileUtils.createOrEmptyDirectory;
import static junit.framework.TestCase.assertTrue;

public class TestSaveSingleChannelTiffSeriesAsTiffStacks
{
    @Test
    public void test( )
    {
        DebugTools.setRootLevel("OFF"); // Bio-Formats

        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory =
                "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-data/nc1-nt3-calibrated-8bit-tiff";

        final String loadingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.tif";

        final Image image = bdp.openImage(
                directory,
                loadingScheme,
                filterPattern );

        // bdp.showImage( image );

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS;
        savingSettings.numIOThreads = 1;
        savingSettings.numProcessingThreads = 4;
        savingSettings.saveProjections = true;

        String outputDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-output/nc1-nt3-calibrated-8bit-tiff-volumes";

        createOrEmptyDirectory( outputDirectory );

        savingSettings.volumesFilePathStump = outputDirectory + "/volume";
        savingSettings.saveVolumes = true;

        outputDirectory = "/Users/tischer/Documents/fiji-plugin-bigDataProcessor2/src/test/resources/test-output/nc1-nt3-calibrated-8bit-tiff-projections";

        createOrEmptyDirectory( outputDirectory );

        savingSettings.projectionsFilePathStump = outputDirectory  + "/projection";


        BigDataProcessor2.saveImageAndWaitUntilDone( savingSettings, image );

        final File testVolumeFile = new File( savingSettings.volumesFilePathStump + "--C00--T00000.tif" );
        final File testProjectionsFile = new File( savingSettings.projectionsFilePathStump + "--xyz-max-projection--C00--T00002.tif" );

        assertTrue( testVolumeFile.exists() );
        assertTrue( testProjectionsFile.exists() );
    }

    public static void main( String[] args )
    {
        new TestSaveSingleChannelTiffSeriesAsLZWTiffStacks().test();
    }

}

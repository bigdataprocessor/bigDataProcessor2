package explore;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.BigDataProcessor2;
import loci.common.DebugTools;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;


/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class ExploreCorrectChromaticShift
{
    //@Test
    public void test()
    {
        DebugTools.setRootLevel("OFF"); // Bio-Formats

        BigDataProcessor2 bdp = new BigDataProcessor2();

        String imageDirectory =
                ExploreCorrectChromaticShift.class
                        .getResource( "/nc2-nt3-calibrated-tiff" ).getFile();

        final Image image = bdp.openImage(
                imageDirectory,
                FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        BigDataProcessor2.showImage( image );
    }

    public static void main( String[] args )
    {
        new ExploreCorrectChromaticShift().test();
    }

}

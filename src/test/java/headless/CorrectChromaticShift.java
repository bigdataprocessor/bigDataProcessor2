package headless;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.process.align.channelshift.AlignChannelsDialog;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewer.ImageViewer;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class CorrectChromaticShift
{
    public static void main(String[] args)
    {
        BigDataProcessor2 bdp = new BigDataProcessor2();

        String imageDirectory =
                CorrectChromaticShift.class
                        .getResource( "/nc2-nt3-calibrated-tiff"  ).getFile();

        final Image image = bdp.openTiffSeries(
                imageDirectory,
                NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        final ImageViewer imageViewer = bdp.showImage( image);

        new AlignChannelsDialog<>( imageViewer );

    }

}

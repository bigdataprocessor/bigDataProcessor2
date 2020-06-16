package playground;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingScheme;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class OpenSingleChannelTiffSeries
{

    public static void main(String[] args)
    {

        String imageDirectory =
                OpenSingleChannelTiffSeries.class
                        .getResource( "/test-data/nc1-nt3-calibrated-16bit-tiff"  ).getFile();

        final Image< ? > image = BigDataProcessor2.openImage(
                imageDirectory,
                NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
                ".*" );

        BigDataProcessor2.showImage( image);



    }

}

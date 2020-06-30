package example;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingSchemes;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class OpenSingleChannelHdf5Series
{
    public static void main(String[] args)
    {
        BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

        String imageDirectory = "/Users/tischer/Documents/isabell-schneider-splitchipmerge/two_channels/stack_0_channel_0";

        final Image image = bigDataProcessor2.openImage(
                imageDirectory,
                NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                ".*" );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSize( 0.13, 0.13, 1.04 );

        bigDataProcessor2.showImage( image);

    }

}

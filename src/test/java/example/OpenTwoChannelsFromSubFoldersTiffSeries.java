package example;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;

public class OpenTwoChannelsFromSubFoldersTiffSeries
{
    public static void main( String[] args )
    {
        BigDataProcessor2 bdp = new BigDataProcessor2();

        String imageDirectory =
                OpenTwoChannelsFromSubFoldersTiffSeries.class
                        .getResource( "/nc2-nt3-calibrated-tiff"  ).getFile();

        final Image image = bdp.openTiffImage(
                imageDirectory,
                FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                ".*" );

        bdp.showImage( image );
    }

}

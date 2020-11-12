package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import org.junit.Test;

import static de.embl.cba.bdp2.open.NamingSchemes.*;

public class TestOpenMultiChannelZLIBCompressedTiffVolumes
{
    public static void main(String[] args)
    {
        new TestOpenMultiChannelZLIBCompressedTiffVolumes().run();
    }

    @Test
    public void run()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6-zlib";

        final Image image = BigDataProcessor2.openTiffSeries( directory, MULTI_CHANNEL_VOLUMES + OME_TIF );
    }
}

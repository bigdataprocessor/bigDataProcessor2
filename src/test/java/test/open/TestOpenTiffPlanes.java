package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import org.junit.Test;

import static de.embl.cba.bdp2.open.core.NamingSchemes.LEICA_LIGHT_SHEET_TIFF;
import static de.embl.cba.bdp2.open.core.NamingSchemes.Z;

public class TestOpenTiffPlanes
{
    public static void main(String[] args)
    {
        new TestOpenTiffPlanes().open();
    }

    @Test
    public void open()
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-planes";

        final Image image = BigDataProcessor2.openImage(
                directory,
                ".*_T(" + NamingSchemes.T + "\\d+)__z(" + Z + "\\d+).*_c(" + NamingSchemes.C + "\\d+).*",
                ".*"
        );

        double[] voxelSize = image.getVoxelSize();
        image.setVoxelSize( voxelSize[ 0 ], voxelSize[ 1 ], 1.0 ); // necessary because voxel size in z is NaN for single plane Tiff

        //BigDataProcessor2.showImage( image, true );
    }
}

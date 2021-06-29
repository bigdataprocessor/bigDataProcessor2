package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.fileseries.OpenSingleTIFFVolumeCommand;
import org.junit.Test;
import test.Utils;

import java.io.File;

import static de.embl.cba.bdp2.open.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class TestOpenSingleTiffVolume
{
    public static void main( String[] args)
    {
        Utils.prepareInteractiveMode();

        new TestOpenSingleTiffVolume().run();
    }

    @Test
    public void run()
    {
        final OpenSingleTIFFVolumeCommand command = new OpenSingleTIFFVolumeCommand();
        command.file = new File( "src/test/resources/test/tiff-nc2-nt2-16bit/volume--C00--T00000.tif" );
        command.run();
    }
}

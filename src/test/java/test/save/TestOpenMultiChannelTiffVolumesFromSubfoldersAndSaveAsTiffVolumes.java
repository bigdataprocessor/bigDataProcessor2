package test.save;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.save.SavingSettings;

public class TestOpenMultiChannelTiffVolumesFromSubfoldersAndSaveAsTiffVolumes
{
    public static void main(String[] args)
    {
        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6-subfolders";

        final Image image = BigDataProcessor2.openImage(
                directory,
                NamingSchemes.MULTI_CHANNEL_VOLUMES_FROM_SUBFOLDERS,
                ".*"
        );

        BigDataProcessor2.showImage( image, true );

        final SavingSettings settings = SavingSettings.getDefaults();
        settings.volumesFilePathStump = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6/image";
        settings.saveFileType = SavingSettings.SaveFileType.TIFF_VOLUMES;
        settings.numIOThreads = 3;
        settings.voxelSpacing = image.getVoxelSpacing();
        settings.voxelUnit = image.getVoxelUnit();
        settings.compression = SavingSettings.COMPRESSION_NONE;

        BigDataProcessor2.saveImage( image, settings, new LoggingProgressListener( "Files saved" ) );
    }
}

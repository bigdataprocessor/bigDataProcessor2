package users.alessandro;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.open.core.NamingScheme;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;

public class OpenAndSaveLargeStacks202002
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openImageFromHdf5( "/Users/tischer/Desktop/2020-02-11_124401/stack_0_channel_4",
				NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
				NamingScheme.PATTERN_LUXENDO_LEFT_CAM,
				"Data"
		);

		BigDataProcessor2.showImage( image);

		final SavingSettings savingSettings = SavingSettings.getDefaults();
		savingSettings.fileType = SavingSettings.FileType.TIFF_VOLUMES;
		savingSettings.numIOThreads = 1;
		savingSettings.numProcessingThreads = 1;
		savingSettings.voxelSpacing = image.getVoxelSpacing();
		savingSettings.voxelUnit = image.getVoxelUnit();

		BigDataProcessor2.saveImage( image, savingSettings, new LoggingProgressListener( "Save" ) );
	}
}

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import ij.ImageJ;

import java.io.File;
import java.io.IOException;

public class ImageCalibrationBdvVisualisationTest
{
	public static void main( String[] args ) throws IOException
	{
		new ImageJ();

		final File file = new File(
				ImageCalibrationBdvVisualisationTest.class.getResource( "nc1-nt1-calibrated-tiff" ).getFile() );

		BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

		String imageDirectory = file.toString();

		final Image image = bigDataProcessor2.openImage(
				imageDirectory.toString(),
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*" );
	}
}

import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.files.FileInfoConstants;
import de.embl.cba.bdp2.viewers.ViewerUtils;

public class TestBdvViewer
{
	public static void main( String[] args )
	{
		BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();

		String imageDirectory = TestBdvViewer.class.getResource( "tiff-nc1-nt2"  ).getFile().toString();

		bigDataProcessor2.openFromDirectory(
				imageDirectory,
				FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
				".*",
				true,
				ViewerUtils.getImageViewer( ViewerUtils.BIG_DATA_VIEWER ) );
	}
}

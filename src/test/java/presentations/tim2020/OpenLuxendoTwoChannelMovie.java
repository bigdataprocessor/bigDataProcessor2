package presentations.tim2020;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.read.NamingScheme;

public class OpenLuxendoTwoChannelMovie
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openHdf5Image( "/Volumes/USB Drive/tim2020/luxendo-two-channel-movie",
				NamingScheme.LOAD_CHANNELS_FROM_FOLDERS,
				NamingScheme.PATTERN_LUXENDO,
				"Data"
		);

		BigDataProcessor2.showImage( image);

	}
}

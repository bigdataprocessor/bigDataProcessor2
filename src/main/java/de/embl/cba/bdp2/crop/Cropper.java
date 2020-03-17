package de.embl.cba.bdp2.crop;

import de.embl.cba.bdp2.image.Image;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Cropper
{
	public static < T extends RealType< T > & NativeType< T > >
	Image< T > crop5D( Image< T > image, Interval intervalXYZCT )
	{
		final IntervalView< T > crop =
				Views.zeroMin( Views.interval( image.getRai(), intervalXYZCT ) );

		final Image< T > croppedImage = image.newImage( crop );
		croppedImage.setName( image.getName() + "-crop" );
		return croppedImage;
	}
}

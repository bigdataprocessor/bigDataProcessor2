package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.sift.SIFTAlignedViews;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

public class TestSIFTAlignment < R extends RealType< R > & NativeType< R > >
{

	@Test
	public void nonVolatileSIFT()
	{
		new ImageJ().ui().showUI();

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/em-2d-sift-align-01",
				FileInfos.TIFF_SLICES,
				".*.tif" );

		final Image< R > alignedImage = SIFTAlignedViews.siftAlignFirstVolume( image, 20 );

		bdp.showImage( alignedImage );

	}

	@Test
	public void volatileSIFT()
	{
		new ImageJ().ui().showUI();

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/em-2d-sift-align-01",
				FileInfos.TIFF_SLICES,
				".*.tif" );


		final Image< R > alignedImage = SIFTAlignedViews.lazySIFTAlignFirstVolume( image, 20 );

		bdp.showImage( alignedImage );

	}

	public static void main( String[] args )
	{
		//new TestSIFTAlignment().nonVolatileSIFT();
		new TestSIFTAlignment().volatileSIFT();
	}

}

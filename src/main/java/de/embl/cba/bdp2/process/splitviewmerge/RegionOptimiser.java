package de.embl.cba.bdp2.process.splitviewmerge;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;

import static de.embl.cba.bdp2.utils.DimensionOrder.C;
import static de.embl.cba.bdp2.utils.DimensionOrder.T;

public class RegionOptimiser
{
	public static < R extends RealType< R > >
	ArrayList< double[] > optimiseCentres2D(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< double[] > calibratedCentres2D,
			double[] calibratedSpan2D,
			double[] voxelSpacing )
	{
		ArrayList< FinalInterval > voxelIntervals =
				SplitViewMergingHelpers.getVoxelntervals5D(
						rai5D, calibratedCentres2D, calibratedSpan2D, voxelSpacing );

		final ArrayList< double[] > centres2D
				= optimiseCentres2D( rai5D, voxelIntervals );

		return centres2D;
	}

	public static < R extends RealType< R > >
	ArrayList< double[] > optimiseCentres2D(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< FinalInterval > voxelIntervals5D )
	{
		final ArrayList< RandomAccessibleInterval< R > > planes
				= new ArrayList<>();

		for ( FinalInterval interval : voxelIntervals5D )
		{
			final IntervalView< R > crop =
					Views.zeroMin(
							Views.interval(
									rai5D,
									interval ) );

			final IntervalView< R > time = Views.hyperSlice( crop, T, 0 );
			final IntervalView< R > channel = Views.hyperSlice( time, C, 0 );
			final IntervalView< R > plane = Views.hyperSlice( channel, SplitViewMerging.Z, channel.dimension( SplitViewMerging.Z ) / 2 );

			ImageJFunctions.show( plane, "" );
			planes.add( plane );
		}


		return null;
	}
}

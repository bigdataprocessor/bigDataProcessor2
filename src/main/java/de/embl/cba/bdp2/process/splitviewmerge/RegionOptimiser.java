package de.embl.cba.bdp2.process.splitviewmerge;

import de.embl.cba.bdp2.tracking.Trackers;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class RegionOptimiser
{
	public static < R extends RealType< R > & NativeType< R > >
	ArrayList< double[] > optimiseCentres2D(
			RandomAccessibleInterval< R > rai5D,
			ArrayList< double[] > calibratedCentres2D,
			double[] calibratedSpan2D,
			double[] voxelSpacing )
	{
		ArrayList< FinalInterval > voxelIntervals =
				SplitViewMergingHelpers.getVoxelntervals5D(
						rai5D, calibratedCentres2D, calibratedSpan2D, voxelSpacing );

		final double[] shift = optimiseCentres2D( rai5D, voxelIntervals );

		System.out.println( "Shift [Voxels]: " + shift[ 0 ] + ", " + shift[ 1 ] );
		System.out.println( "Shift [Calibrated]: "
				+ shift[ 0 ] * voxelSpacing[ 0 ] + ", "
				+ shift[ 1 ] * voxelSpacing[ 1 ]);

		final ArrayList< double[] > newCentres = new ArrayList<>();
		newCentres.add( calibratedCentres2D.get( 0 ) );

		final double[] newCentre = new double[ 2 ];
		final double[] oldCentre = calibratedCentres2D.get( 1 );
		for ( int d = 0; d < 2; d++ )
			newCentre[ d ] = oldCentre[ d ] - shift[ d ] * voxelSpacing[ d ];

		newCentres.add( newCentre );

		return newCentres;
	}

	private static < R extends RealType< R > & NativeType< R > >
	double[] optimiseCentres2D(
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
			final IntervalView< R > plane =
					Views.hyperSlice(
							channel,
							Z,
							channel.dimension( Z ) / 2 );

			planes.add( plane );
		}

		final double[] shift = Trackers.getPhaseCorrelationShift(
				planes.get( 0 ), planes.get( 1 ),
				Executors.newFixedThreadPool( 2 ) );


		return shift;
	}
}

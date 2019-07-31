package de.embl.cba.bdp2.tracking;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.phasecorrelation.PhaseCorrelation2;
import net.imglib2.algorithm.phasecorrelation.PhaseCorrelationPeak2;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static de.embl.cba.transforms.utils.ImageCreators.createEmptyArrayImg;

public class PhaseCorrelationTranslationComputer
{

	public static < R extends RealType< R > & NativeType< R > >
	double[] computeShift(
			RandomAccessibleInterval< R > input0,
			RandomAccessibleInterval< R > input1,
			ExecutorService executorService )
	{
		final double[] sigmas = {5,5};

		final ArrayList< RandomAccessibleInterval< R > > inputs = new ArrayList<>();
		inputs.add( input0 );
		inputs.add( input1 );

//		final ArrayList< RandomAccessibleInterval< R > > processed = new ArrayList<>();
//
//		for ( RandomAccessibleInterval< R > input : inputs )
//		{
//			RandomAccessibleInterval< R > blur = createEmptyArrayImg( input );
//			Gauss3.gauss( sigmas, Views.extendBorder( input ), blur ) ;
//			RandomAccessibleInterval< R > image = Views.zeroMin( blur );
//			processed.add( image );
//			//ImageJFunctions.replaceImage( image );
//		}

		RandomAccessibleInterval< FloatType > pcm =
				PhaseCorrelation2.calculatePCM(
						inputs.get( 0 ),
						inputs.get( 1 ),
						new CellImgFactory(new FloatType()),
						new FloatType(),
						new CellImgFactory(new ComplexFloatType()),
						new ComplexFloatType(),
						executorService );

		//ImageJFunctions.replaceImage( pcm, "corr" );

		final long minOverlap = ( long ) ( 0.5 * input0.dimension( 0 ) * input0.dimension( 1 ) );

		PhaseCorrelationPeak2 shiftPeak =
				PhaseCorrelation2.getShift(
						pcm,
						inputs.get( 0 ), // needed for cross-correlation confirmation
						inputs.get( 1 ), // needed for cross-correlation confirmation
						10,
						minOverlap,
						true,
						false,
						executorService );

		double[] shift = new double[ input0.numDimensions() ];

		shiftPeak.getSubpixelShift().localize(shift);

		return shift;
	}
}

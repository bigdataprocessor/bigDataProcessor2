package de.embl.cba.bigDataTools2.process;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.Random;

public class LazyDownsampler < R extends RealType< R > & NativeType< R > >
{

	final RandomAccessibleInterval< R > rai;
	final int span;

	public LazyDownsampler( RandomAccessibleInterval< R > rai, int span )
	{
		this.rai = rai;
		this.span = span;
	}

	public  RandomAccessibleInterval< R > get()
	{
		return Views.subsample( averageView( rai, span ), span );
	}

	private RandomAccessibleInterval< R > averageView(
			RandomAccessibleInterval< R > rai,
			int span )
	{
		Shape shape = new RectangleShape( span, false );

		final RandomAccessible< Neighborhood< R > > nra =
				shape.neighborhoodsRandomAccessible( Views.extendBorder( rai ) );

		final RandomAccessibleInterval< Neighborhood< R > > nrai = Views.interval( nra, rai );

		final RandomAccessibleInterval< R > averageView =
				Converters.convert( nrai,
						( neighborhood, output ) ->
						{
							setNeighborhoodAverage( neighborhood, output );
						},
						Util.getTypeFromInterval( rai ) );

		return averageView;
	}

	private void setNeighborhoodAverage( Neighborhood< R > neighborhood, R output )
	{
		double sum = 0;

		for ( R value : neighborhood )
			sum += value.getRealDouble();

		output.setReal( sum / neighborhood.size() );
	}

}

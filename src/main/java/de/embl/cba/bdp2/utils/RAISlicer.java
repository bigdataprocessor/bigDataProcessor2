/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.utils;

import de.embl.cba.bdp2.log.Logger;
import net.imglib2.*;
import net.imglib2.algorithm.util.Grids;
import net.imglib2.img.AbstractImg;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.List;

import static de.embl.cba.bdp2.open.CacheUtils.MAX_ARRAY_LENGTH;
import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public class RAISlicer
{
	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > getSliceView(
			RandomAccessibleInterval< R > image,
			long z,
			long c,
			long t )
	{
		long[] minInterval = new long[]{
				image.min( X ),
				image.min( Y ),
				z,
				c,
				t };

		long[] maxInterval = new long[]{
				image.max( X ),
				image.max( Y ),
				z,
				c,
				t };

		RandomAccessibleInterval raiXY =
				Views.dropSingletonDimensions(
						Views.interval( image, minInterval, maxInterval ) );

		return raiXY;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > getVolumeView(
			RandomAccessibleInterval< R > raiXYZCT,
			long c,
			long t )
	{
		long[] minInterval = new long[]{
				raiXYZCT.min( X ),
				raiXYZCT.min( Y ),
				raiXYZCT.min( Z ),
				c,
				t };

		long[] maxInterval = new long[]{
				raiXYZCT.max( X ),
				raiXYZCT.max( Y ),
				raiXYZCT.max( Z ),
				c,
				t };

		RandomAccessibleInterval< R > raiXYZ =
				Views.dropSingletonDimensions(
						Views.interval( raiXYZCT, minInterval, maxInterval ) );

		return raiXYZ;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > getFrameView(
			RandomAccessibleInterval< R > rai,
			long t )
	{
//		long[] minInterval = new long[]{
//				image.min( X ),
//				image.min( Y ),
//				image.min( Z ),
//				image.min( C ),
//				t };
//
//		long[] maxInterval = new long[]{
//				image.max( X ),
//				image.max( Y ),
//				image.max( Z ),
//				image.max( C ),
//				t };

//		RandomAccessibleInterval rai = Views.interval( image, minInterval, maxInterval );

		final IntervalView< R > frame = Views.hyperSlice( rai, T, t );

		return frame;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > createVolumeCopy(
			RandomAccessibleInterval< R > raiXYZCT,
			long c,
			long t,
			int numThreads,
			R type )
	{
		long start = System.currentTimeMillis();

		long[] minInterval = new long[]{
				raiXYZCT.min( X ),
				raiXYZCT.min( Y ),
				raiXYZCT.min( Z ),
				c,
				t };

		long[] maxInterval = new long[]{
				raiXYZCT.max( X ),
				raiXYZCT.max( Y ),
				raiXYZCT.max( Z ),
				c,
				t };

		final IntervalView< R > interval = Views.interval( raiXYZCT, minInterval, maxInterval );
		RandomAccessibleInterval< R > raiXYZ = Views.dropSingletonDimensions( interval );

		// force into RAM
		raiXYZ = copyVolumeRAI( raiXYZ, numThreads, type );

		Logger.benchmark( "Processed volume, using " + numThreads + " thread(s) in [ ms ]: " + ( System.currentTimeMillis() - start ) );

		return raiXYZ;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > createPlaneCopy(
			RandomAccessibleInterval< R > rai,
			Interval interval,
			R type,
			long z,
			long c,
			long t )
	{

		long[] minInterval = new long[]{
				interval.min( X ),
				interval.min( Y ),
				z,
				c,
				t };

		long[] maxInterval = new long[]{
				interval.max( X ),
				interval.max( Y ),
				z,
				c,
				t };

		// Accommodate cases where the asked-for volume is out-of-bounds
		RandomAccessible< R > extended = Views.extendBorder( rai );

		RandomAccessibleInterval< R > plane =
				Views.zeroMin(
						Views.dropSingletonDimensions(
								Views.interval( extended, minInterval, maxInterval ) ) );

		final ArrayImg copy = new ArrayImgFactory( type ).create( plane );

		copy( plane, copy );

		return copy;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > copyVolumeRAI( RandomAccessibleInterval< R > volume, int numThreads, R type )
	{
		final int dimensionX = ( int ) volume.dimension( 0 );
		final int dimensionY = ( int ) volume.dimension( 1 );
		final int dimensionZ = ( int ) volume.dimension( 2 );

		final long numElements = AbstractImg.numElements( Intervals.dimensionsAsLongArray( volume ) );

		RandomAccessibleInterval< R > copy;

		if ( numElements < MAX_ARRAY_LENGTH )
		{
			copy = new ArrayImgFactory( type ).create( volume );
		}
		else
		{
			int nz = (int) ( (long) MAX_ARRAY_LENGTH / ( volume.dimension( 0  ) * volume.dimension( 1 ) ) );

			final int[] cellSize = {
					dimensionX,
					dimensionY,
					nz };

			copy = new CellImgFactory( type, cellSize ).create( volume );
		}

		final int blockSizeZ = ( int ) Math.ceil( 1.0D * dimensionZ / numThreads );

		if ( blockSizeZ <= 0 )
		{
			throw new UnsupportedOperationException( "The block size in z must be > 0 but is " + blockSizeZ + "; dimensionZ = " + dimensionZ );
		}

		final int[] blockSize = {
				dimensionX,
				dimensionY,
				blockSizeZ };

		final List< Interval > intervals = Grids.collectAllContainedIntervals( Intervals.dimensionsAsLongArray( volume ), blockSize );

		intervals.parallelStream().forEach( interval -> copy( volume, Views.interval( copy, interval ) ) );

		return copy;
	}

	private static < R extends RealType< R > & NativeType< R > >
	R getType( RandomAccessibleInterval< R > volume )
	{
		R type = null;
		try
		{
			type = Util.getTypeFromInterval( volume );
		}
		catch ( Exception e )
		{
			System.err.println( e );
		}
		return type;
	}


	public static < T extends Type< T > > void copy(
			final RandomAccessible< T > source,
			final IterableInterval< T > target )
	{
		// create a cursor that automatically localizes itself on every move
		Cursor< T > targetCursor = target.localizingCursor();
		RandomAccess< T > sourceRandomAccess = source.randomAccess();

		while ( targetCursor.hasNext() )
		{
			targetCursor.fwd();
			targetCursor.get().set( sourceRandomAccess.setPositionAndGet( targetCursor ) );
		}
	}

}

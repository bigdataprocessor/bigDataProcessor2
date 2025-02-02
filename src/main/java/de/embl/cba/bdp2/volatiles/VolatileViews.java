/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
package de.embl.cba.bdp2.volatiles;

import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.cache.SharedQueue;
import bdv.util.volatiles.VolatileRandomAccessibleIntervalView;
import bdv.util.volatiles.VolatileRandomAccessibleView;
import bdv.util.volatiles.VolatileTypeMatcher;
import bdv.util.volatiles.VolatileViewData;
import de.embl.cba.bdp2.devel.register.HypersliceTransformProvider;
import de.embl.cba.bdp2.devel.register.TransformedStackView;
import de.embl.cba.lazyalgorithm.converter.NeighborhoodAverageConverter;
import de.embl.cba.lazyalgorithm.converter.VolatileNeighborhoodAverageConverter;
import de.embl.cba.neighborhood.RectangleShape2;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhoodRandomAccess;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.ref.WeakRefVolatileCache;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.CreateInvalid;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.cache.volatiles.VolatileCache;
import net.imglib2.converter.Converter;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.basictypeaccess.volatiles.VolatileArrayDataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.imglib2.img.basictypeaccess.AccessFlags.DIRTY;
import static net.imglib2.img.basictypeaccess.AccessFlags.VOLATILE;

/*
 * Wrap view cascades ending in {@link CachedCellImg} as volatile views.
 * {@link RandomAccessible}s wrapped in this way can be displayed in
 * BigDataViewer while load asynchronously.
 *
 * @author Tobias Pietzsch
 * @author Christian Tischer
 */
public class VolatileViews
{
	public static < T extends Type< T > & NativeType< T >, V extends Volatile< T > >
	RandomAccessibleInterval< V > wrapAsVolatile(
			final RandomAccessibleInterval< T > rai )
	{
		return wrapAsVolatile( rai, null, null );
	}

	public static < T extends Type< T > & NativeType< T >, V extends Volatile< T > >
	RandomAccessibleInterval< V > wrapAsVolatile(
			final RandomAccessibleInterval< T > rai,
			final SharedQueue queue )
	{
		return wrapAsVolatile( rai, queue, null );
	}

	public static < T extends Type< T > & NativeType< T >, V extends Volatile< T > >
	RandomAccessibleInterval< V > wrapAsVolatile(
			final RandomAccessibleInterval< T > rai,
			final SharedQueue queue,
			final CacheHints hints )
	{
		final VolatileViewData< T, V > viewData =
				wrapAsVolatileViewData( rai, queue, hints );
		return new VolatileRandomAccessibleIntervalView<>( viewData );
	}

	public static < T extends Type< T > & NativeType< T >, V extends Volatile< T > > RandomAccessible< V > wrapAsVolatile(
			final RandomAccessible< T > rai )
	{
		return wrapAsVolatile( rai, null, null );
	}

	public static < T extends Type< T > & NativeType< T >, V extends Volatile< T > > RandomAccessible< V > wrapAsVolatile(
			final RandomAccessible< T > rai,
			final SharedQueue queue )
	{
		return wrapAsVolatile( rai, queue, null );
	}

	public static < T extends Type< T > & NativeType< T >, V extends Volatile< T > >
	RandomAccessible< V > wrapAsVolatile(
			final RandomAccessible< T > rai,
			final SharedQueue queue,
			final CacheHints hints )
	{
		@SuppressWarnings( "unchecked" )
		final VolatileViewData< T, V > viewData = ( VolatileViewData< T, V > )
				wrapAsVolatileViewData( rai, queue, hints );
		return new VolatileRandomAccessibleView<>( viewData );
	}

	// ==============================================================

	@SuppressWarnings( "unchecked" )
	private static < T extends NativeType< T >,
			S extends NativeType< S >,
			V extends Volatile< T >,
			R extends RealType< R > >
	VolatileViewData< T, V > wrapAsVolatileViewData(
			final RandomAccessible< T > rai,
			final SharedQueue queue,
			final CacheHints hints )
	{
		if ( rai instanceof CachedCellImg )
		{
			@SuppressWarnings( "rawtypes" )
			final VolatileViewData< T, V > volatileViewData = wrapCachedCellImg( ( CachedCellImg ) rai, queue, hints );
            return volatileViewData;
		}
		else if ( rai instanceof IntervalView )
		{
			final IntervalView< T > view = ( IntervalView< T > ) rai;
			final VolatileViewData< T, V > sourceData =
					wrapAsVolatileViewData( view.getSource(), queue, hints );

            return new VolatileViewData<>(
					new IntervalView<>( sourceData.getImg(), view ),
					sourceData.getCacheControl(),
					sourceData.getType(),
					sourceData.getVolatileType() );
		}
		else if ( rai instanceof MixedTransformView )
		{
			final MixedTransformView< T > view = ( MixedTransformView< T > ) rai;

			final VolatileViewData< T, V > sourceData =
					wrapAsVolatileViewData( view.getSource(), queue, hints );

            return new VolatileViewData<>(
					new MixedTransformView<>( sourceData.getImg(), view.getTransformToSource() ),
					sourceData.getCacheControl(),
					sourceData.getType(),
					sourceData.getVolatileType() );
		}
		else if ( rai instanceof SubsampleIntervalView )
		{
			final SubsampleIntervalView< T > view = ( SubsampleIntervalView< T > ) rai;
			final VolatileViewData< T, V > sourceData =
					wrapAsVolatileViewData( view.getSource(), queue, hints );

			final VolatileViewData< T, V > volatileViewData = new VolatileViewData<>(
					new SubsampleIntervalView<>(
							( RandomAccessibleInterval< V > ) sourceData.getImg(),
							view.getSteps() ),
						sourceData.getCacheControl(),
						sourceData.getType(),
						sourceData.getVolatileType() );

			return volatileViewData;
		}
		else if ( rai instanceof SubsampleView ) // TODO: do we need this ?
		{
			final SubsampleView< T > view = ( SubsampleView< T > ) rai;
			final VolatileViewData< T, V > sourceData =
					wrapAsVolatileViewData( view.getSource(), queue, hints );

			final VolatileViewData< T, V > volatileViewData = new VolatileViewData<>(
					new SubsampleView<>( sourceData.getImg(), view.getSteps() ),
						sourceData.getCacheControl(),
						sourceData.getType(),
						sourceData.getVolatileType() );

			return volatileViewData;
		}
		else if ( rai instanceof ConvertedRandomAccessibleInterval )
		{
			final ConvertedRandomAccessibleInterval< T, S > view
					= ( ConvertedRandomAccessibleInterval< T, S > ) rai;

			final VolatileViewData< T, V > vViewData =
					wrapAsVolatileViewData( view.getSource(), queue, hints );

			final S destinationType = view.getDestinationSupplier().get();

			final NativeType< ? > volatileDestinationType =
					VolatileTypeMatcher.getVolatileTypeForType( destinationType );

			final RandomAccessibleInterval< V > vRAI =
					( RandomAccessibleInterval< V > ) vViewData.getImg();

			final Converter< ? super T, ? super S > converter = view.getConverterSupplier().get();

			if ( converter instanceof NeighborhoodAverageConverter )
			{
				final VolatileNeighborhoodAverageConverter< R > vConverter =
						new VolatileNeighborhoodAverageConverter<>();

				final ConvertedRandomAccessibleInterval converted
						= new ConvertedRandomAccessibleInterval(
						vRAI,
						vConverter,
						volatileDestinationType::copy );

				final VolatileViewData volatileViewData = new VolatileViewData(
						converted,
						vViewData.getCacheControl(),
						destinationType,
						( Volatile ) volatileDestinationType // TODO: can one avoid cast?
				);

				return volatileViewData;
			}
			else
			{
				Converter< V, Volatile< S > > volatileConverter
						= (vt, vu) -> {
					boolean isValid = vt.isValid();
					vu.setValid(isValid);
					if (isValid) {
						converter.convert(vt.get(), vu.get());
					}
				};

				final ConvertedRandomAccessibleInterval converted
						= new ConvertedRandomAccessibleInterval(
						vRAI,
						volatileConverter,
						volatileDestinationType::copy );

				final VolatileViewData volatileViewData = new VolatileViewData(
						converted,
						vViewData.getCacheControl(),
						destinationType,
						( Volatile ) volatileDestinationType );

				return volatileViewData;
			}

		}
		else if ( rai instanceof StackView )
		{
			final StackView< T > view =
					( StackView< T > ) rai;

			final List< RandomAccessibleInterval< T > > slices = view.getSourceSlices();

			final List< VolatileViewData< T, V > > volatileViews = new ArrayList<>();
			for ( RandomAccessibleInterval< T > sourceSlice : slices )
			{
				volatileViews.add(
						wrapAsVolatileViewData( sourceSlice, queue, hints ));
			}

			final List< RandomAccessible< V > > volatileSlices = new ArrayList<>();
			for ( VolatileViewData< T, V > volatileViewData : volatileViews )
			{
				volatileSlices.add( volatileViewData.getImg() );
			}

			/*
			TODO:
			It works but it feels wrong just taking the first slice to get the
			cacheControl, type and volatileType.
			How to do this properly?
			 */
			final VolatileViewData volatileViewData = new VolatileViewData(
					new StackView( volatileSlices ),
					volatileViews.get( 0 ).getCacheControl(),
					volatileViews.get( 0 ).getType(),
					volatileViews.get( 0 ).getVolatileType() );

			return volatileViewData;
		}
		else if ( rai instanceof TransformedStackView )
		{
			final TransformedStackView< T > view =
					( TransformedStackView< T > ) rai;

			final List< RandomAccessibleInterval< T > > slices = view.getHyperslices();
			final HypersliceTransformProvider transformProvider = view.getTransformProvider();

			final List< VolatileViewData< T, V > > volatileViews = new ArrayList<>();
			for ( RandomAccessibleInterval< T > sourceSlice : slices )
				volatileViews.add(
						wrapAsVolatileViewData( sourceSlice, queue, hints ));


			final List< RandomAccessible< V > > volatileSlices = new ArrayList<>();
			for ( VolatileViewData< T, V > volatileViewData : volatileViews )
				volatileSlices.add( volatileViewData.getImg() );

			/*
			TODO:
			It works but it feels wrong just taking the first slice to get the
			cacheControl, type and volatileType.
			How to do this properly?
			 */
			final VolatileViewData volatileViewData = new VolatileViewData(
					new TransformedStackView( volatileSlices, transformProvider ),
					volatileViews.get( 0 ).getCacheControl(),
					volatileViews.get( 0 ).getType(),
					volatileViews.get( 0 ).getVolatileType() );

			return volatileViewData;
		}
		else if ( rai instanceof RectangleShape2.NeighborhoodsAccessible )
		{
			/*
			RectangleShape2 is a modified version of the original Rectangle shape:
			1. it allows for non-cubic rectangles
			2. it can return its span and factory => can be used here to reconstruct itself.
			 */

			// TODO: I did not manage to put the typing here
			final RectangleShape2.NeighborhoodsAccessible view =
					( RectangleShape2.NeighborhoodsAccessible ) rai;

			final VolatileViewData< T, V > sourceData =
					wrapAsVolatileViewData( view.getSource(), queue, hints );

			final VolatileViewData volatileViewData = new VolatileViewData(
					new RectangleShape2.NeighborhoodsAccessible(
							sourceData.getImg(),
							view.getSpan(),
							view.getFactory() ),
					sourceData.getCacheControl(),
					sourceData.getType(),
					sourceData.getVolatileType() );

			return volatileViewData;
		}
		else if ( rai instanceof ExtendedRandomAccessibleInterval )
		{
			// TODO: I did not manage to put the typing here
			final ExtendedRandomAccessibleInterval view =
					( ExtendedRandomAccessibleInterval ) rai;

			final VolatileViewData< T, V > sourceData = wrapAsVolatileViewData(
					view.getSource(), queue, hints );

			final VolatileViewData volatileViewData = new VolatileViewData(
					new ExtendedRandomAccessibleInterval(
							// TODO: can we avoid the casting?
							( RandomAccessibleInterval< V > ) sourceData.getImg(),
							view.getOutOfBoundsFactory() ),
					sourceData.getCacheControl(),
					sourceData.getType(),
					sourceData.getVolatileType() );

			return volatileViewData;
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	private static < T extends NativeType< T >, V extends Volatile< T > >
	boolean isNeighborhood( RandomAccessibleInterval< V > vRAI )
	{
		final RandomAccess< V > vRandomAccess = vRAI.randomAccess();

		// TODO: make more general
        return vRandomAccess instanceof RectangleNeighborhoodRandomAccess;
	}

	@SuppressWarnings( "unchecked" )
	private static < T extends NativeType< T >, V extends Volatile< T > & NativeType< V >, A extends DataAccess> VolatileViewData< T, V > wrapCachedCellImg(
			final CachedCellImg< T, A > cachedCellImg,
			SharedQueue queue,
			CacheHints hints )
	{
		final T type = cachedCellImg.createLinkedType();
		final CellGrid grid = cachedCellImg.getCellGrid();
		final Cache< Long, Cell< A > > cache = cachedCellImg.getCache();

		final Set< AccessFlags > flags = AccessFlags.ofAccess( cachedCellImg.getAccessType() );
		if ( !flags.contains( VOLATILE ) )
			throw new IllegalArgumentException( "underlying " + CachedCellImg.class.getSimpleName() + " must have volatile access type" );
		final boolean dirty = flags.contains( DIRTY );

		final V vtype = ( V ) VolatileTypeMatcher.getVolatileTypeForType( type );
		if ( queue == null )
			queue = new SharedQueue( 1, 1 );
		if ( hints == null )
			hints = new CacheHints( LoadingStrategy.VOLATILE, 0, false );
		@SuppressWarnings( "rawtypes" )
		final VolatileCachedCellImg< V, ? > img = createVolatileCachedCellImg( grid, vtype, dirty, ( Cache ) cache, queue, hints );

		return new VolatileViewData<>( img, queue, type, vtype );
	}

	private static < T extends NativeType< T >, A extends VolatileArrayDataAccess< A > > VolatileCachedCellImg< T, A > createVolatileCachedCellImg(
			final CellGrid grid,
			final T type,
			final boolean dirty,
			final Cache< Long, Cell< A > > cache,
			final SharedQueue queue,
			final CacheHints hints )
	{
		final CreateInvalid< Long, Cell< A > > createInvalid = CreateInvalidVolatileCell.get( grid, type, dirty );
		final VolatileCache< Long, Cell< A > > volatileCache = new WeakRefVolatileCache<>( cache, queue, createInvalid );
        return new VolatileCachedCellImg<>( grid, type, hints, volatileCache );
	}
}

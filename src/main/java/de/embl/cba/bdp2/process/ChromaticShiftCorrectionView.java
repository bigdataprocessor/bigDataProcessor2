package de.embl.cba.bdp2.process;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.logging.ImageJLogger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.lazyalgorithm.LazyDownsampler;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChromaticShiftCorrectionView< T extends RealType< T > & NativeType< T > > extends JFrame
{
	public ChromaticShiftCorrectionView( final ImageViewer< T > imageViewer  )
	{
		final RandomAccessibleInterval< T > rai = imageViewer.getRai();

		long[] span = new long[]{0,0,0,0,0};

		final RandomAccessibleInterval< T > downSampledView =
				new LazyDownsampler<>( rai, span ).getDownsampledView();

		ImageViewer newImageViewer = imageViewer.newImageViewer();

		final double[] originalVoxelSize = imageViewer.getVoxelSize();

		final AffineTransform3D transform3D = imageViewer.getViewerTransform().copy();

		newImageViewer.show(
				downSampledView,
				"binned view",
				getBinnedVoxelSize( span, originalVoxelSize ),
				imageViewer.getCalibrationUnit(),
				true);

		ImageJLogger.info( "Binned view size [GB]: "
				+ Utils.getSizeGB( downSampledView ) );

		newImageViewer.setViewerTransform( transform3D );

		newImageViewer.addMenus( new BdvMenus() );

		showChromaticShiftCorrectionDialog( rai, originalVoxelSize, newImageViewer, span );

	}

	private double[] getBinnedVoxelSize( long[] span, double[] voxelSize )
	{
		final double[] newVoxelSize = new double[ voxelSize.length ];

		for ( int d = 0; d < 3; d++ )
			newVoxelSize[ d ] = voxelSize[ d ] * ( 2 * span[ d ] + 1 );

		return newVoxelSize;
	}

	private void showChromaticShiftCorrectionDialog(
			RandomAccessibleInterval< T > rai,
			double[] originalVoxelSize,
			ImageViewer imageViewer,
			long[] span )
	{
		final JFrame frame = new JFrame( "Binning" );

		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		final ArrayList< BoundedValue > boundedValues = new ArrayList<>();
		final ArrayList< SliderPanel > sliderPanels = new ArrayList<>();

		rai.dimensions( DimensionOrder.C );

		for ( int d = 0; d < 3; d++ )
		{
			boundedValues.add( new BoundedValue(
					1,
					11,
					( int ) ( 2 * span[ d ] + 1 ) ) );

			sliderPanels.add(
					new SliderPanel(
							"Binning, dimension " + d ,
								boundedValues.get( d ),
								2 ));
		}



		class UpdateListener implements BoundedValue.UpdateListener
		{
			private long[] previousSpan;

			@Override
			public synchronized void update()
			{
				final long[] span = new long[ 5 ];

				for ( int d = 0; d < 3; d++ )
					span[ d ] = ( boundedValues.get( d ).getCurrentValue() - 1 ) / 2;

				boolean spanChanged = false;
				if ( previousSpan != null )
				{
					for ( int d = 0; d < 3; d++ )
					{
						if ( span[ d ] != previousSpan[ d ] )
						{
							sliderPanels.get( d ).update();
							previousSpan[ d ] = span[ d ];
							spanChanged = true;
						}
					}
				}
				else
				{
					spanChanged = true;
				}

				if ( ! spanChanged ) return;

				final RandomAccessibleInterval< T > downSampleView =
						new LazyDownsampler<>( rai, span ).getDownsampledView();

				final double[] binnedVoxelSize = getBinnedVoxelSize( span, originalVoxelSize );

				final AffineTransform3D transform3D = imageViewer.getViewerTransform().copy();

				imageViewer.show(
						downSampleView,
						imageViewer.getImageName(),
						binnedVoxelSize,
						imageViewer.getCalibrationUnit(),
						true );

				ImageJLogger.info( "Binned view size [GB]: "
						+ Utils.getSizeGB( downSampleView ) );

				imageViewer.setViewerTransform( transform3D );


			}
		}

		final UpdateListener updateListener = new UpdateListener();

		for ( int d = 0; d < 3; d++ )
		{
			boundedValues.get( d ).setUpdateListener( updateListener );
			panel.add( sliderPanels.get( d ) );
		}


		frame.setContentPane( panel );
		frame.setBounds(
				MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		frame.setResizable( false );
		frame.pack();
		frame.setVisible( true );
	}


}

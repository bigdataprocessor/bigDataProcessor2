package de.embl.cba.bdp2.process;

import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValueDouble;

import static de.embl.cba.bdp2.ui.BigDataProcessorCommand.logger;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import ij.IJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

import javax.swing.*;
import java.awt.*;

public class UnsignedByteTypeConversion< T extends RealType< T > >
{

	public UnsignedByteTypeConversion( final ImageViewer imageViewer  )
	{

		final RandomAccessibleInterval rai = imageViewer.getImage().getRai();

		if ( ( Util.getTypeFromInterval( rai ) instanceof UnsignedByteType) )
		{
			IJ.showMessage("This image is already 8-bit.");
			return;
		}

		final double mapTo0 = imageViewer.getDisplaySettings( 0 ).getMinValue();
		final double mapTo255 = imageViewer.getDisplaySettings( 0 ).getMaxValue();

		final RealUnsignedByteConverter< T > converter =
				new RealUnsignedByteConverter<>(
						mapTo0,
						mapTo255 );

		final RandomAccessibleInterval converted =
				Converters.convert(
					rai,
					converter,
					new UnsignedByteType() );

		ImageViewer newImageViewer = imageViewer.newImageViewer();

		newImageViewer.show(
				converted,
				FileInfos.UNSIGNED_BYTE_VIEW_NAME,
				imageViewer.getImage().getVoxelSpacing(),
				imageViewer.getImage().getVoxelUnit(),
				true);

		logger.info( "8-bit view size [GB]: " + Utils.getSizeGB( converted ) );

		newImageViewer.addMenus( new BdvMenus() );

		showConversionAdjustmentDialog( converter, mapTo0, mapTo255, newImageViewer );

	}

	private void showConversionAdjustmentDialog(
			RealUnsignedByteConverter< T > converter,
			double currentMin,
			double currentMax,
			ImageViewer imageViewer)
	{
		final double rangeMin = 0;
		final double rangeMax = 65535;

		final JFrame frame = new JFrame( "8-bit conversion" );

		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		final BoundedValueDouble min = new BoundedValueDouble(
				rangeMin,
				rangeMax,
				currentMin );

		final BoundedValueDouble max = new BoundedValueDouble(
				rangeMin,
				rangeMax,
				currentMax );

		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		final SliderPanelDouble minSlider =
				new SliderPanelDouble( "0    <= ", min, 1 );
		final SliderPanelDouble maxSlider =
				new SliderPanelDouble( "255  <= ", max, 1 );

		class UpdateListener implements BoundedValueDouble.UpdateListener
		{
			@Override
			public void update()
			{
				converter.setMin( min.getCurrentValue() );
				converter.setMax( max.getCurrentValue() );
				minSlider.update();
				maxSlider.update();
				imageViewer.repaint();
			}
		}

		final UpdateListener updateListener = new UpdateListener();
		min.setUpdateListener( updateListener );
		max.setUpdateListener( updateListener );

		panel.add( minSlider );
		panel.add( maxSlider );

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

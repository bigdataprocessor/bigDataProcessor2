package de.embl.cba.bdp2.process.crop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.ui.AbstractOpenCommand;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.util.List;

public class CropDialog< R extends RealType< R > & NativeType< R > >
{
	public static final String SHOW_IN_VOXEL_UNITS = "Use voxel units";
	public static final String SHOW_IN_CALIBRATED_UNITS = "Use calibrated units";
	private String unitsChoice = SHOW_IN_CALIBRATED_UNITS;

	public static boolean askForUnitsChoice = true;
	private final ImageViewer< R > viewer;

	public CropDialog( ImageViewer< R > viewer )
	{
		this.viewer = viewer;
	}

	public void showDialog()
	{
		if ( askForUnitsChoice )
		{
			final GenericDialog gd = new GenericDialog( "Cropping Units" );
			gd.addChoice( "Cropping dialog", new String[]{ SHOW_IN_VOXEL_UNITS, SHOW_IN_CALIBRATED_UNITS }, unitsChoice );
			gd.addCheckbox( "Do not ask again during this session", true );
			gd.showDialog();
			if ( gd.wasCanceled() ) return;
			unitsChoice = gd.getNextChoice();
			askForUnitsChoice = ! gd.getNextBoolean();
		}

		if ( unitsChoice.equals( SHOW_IN_VOXEL_UNITS ) )
		{
			showDialog( viewer, false );
		}
		else if ( unitsChoice.equals( SHOW_IN_CALIBRATED_UNITS ) )
		{
			showDialog( viewer, true );
		}
	}

	private void showDialog( ImageViewer< R > viewer, boolean calibratedUnits )
	{
		final Image< R > inputImage = viewer.getImage();

		final Interval voxelInterval;
		final RealInterval realInterval;
		if ( calibratedUnits )
		{
			realInterval = viewer.getRealIntervalXYZCTViaDialog();
			if ( realInterval == null ) return;
			voxelInterval = toVoxelInterval( realInterval, inputImage.getVoxelSize() );
		}
		else
		{
			realInterval = null;
			voxelInterval = viewer.getVoxelIntervalXYZCTViaDialog();
			if ( voxelInterval == null ) return;
		}


		Image< R > outputImage = BigDataProcessor2.crop( inputImage, voxelInterval );

		SwingUtilities.invokeLater( () -> {
			log( calibratedUnits, voxelInterval, realInterval, outputImage );
			recordMacro( inputImage, outputImage, voxelInterval );
		} );


		final ImageViewer< R > newViewer = BigDataProcessor2.showImage( outputImage, false );
		final List< DisplaySettings > displaySettings = viewer.getDisplaySettings();
		newViewer.setDisplaySettings( displaySettings );
	}

	public void log( boolean calibratedUnits, Interval voxelInterval, RealInterval realInterval, Image< R > outputImage )
	{
		Logger.info( "\n# Crop" );
		if ( calibratedUnits )
			Logger.info( "Crop interval [calibrated]: " + realInterval.toString() );
		Logger.info( "Crop interval [voxels]: " + voxelInterval.toString() );
		Logger.info( "Crop view size [GB]: " + Utils.getSizeGB( outputImage.getRai() ) );
	}

	public static Interval toVoxelInterval(
			RealInterval intervalXYZCT,
			double[] voxelSize )
	{
		final long[] min = new long[ intervalXYZCT.numDimensions() ];
		final long[] max = new long[ intervalXYZCT.numDimensions() ];

		// XYZ
		for ( int d = 0; d < 3; d++ )
		{
			min[ d ] = (long) ( intervalXYZCT.realMin( d ) / voxelSize[ d ] );
			max[ d ] = (long) ( intervalXYZCT.realMax( d ) / voxelSize[ d ] );
		}

		// CT
		for ( int d = 3; d < 5; d++ )
		{
			min[ d ] = (long) ( intervalXYZCT.realMin( d ) );
			max[ d ] = (long) ( intervalXYZCT.realMax( d ) );
		}

		return new FinalInterval( min, max );
	}


	private void recordMacro( Image< R > inputImage, Image< R > outputImage, Interval intervalXYZCT )
	{
		final MacroRecorder< R > recorder = new MacroRecorder<>( CropCommand.COMMAND_FULL_NAME, inputImage, outputImage, AbstractOpenCommand.SHOW_IN_NEW_VIEWER );
		
		recorder.addOption( "minX", intervalXYZCT.min(0 ) );
		recorder.addOption( "minY", intervalXYZCT.min(1 ) );
		recorder.addOption( "minZ", intervalXYZCT.min(2 ) );
		recorder.addOption( "minC", intervalXYZCT.min(3 ) );
		recorder.addOption( "minT", intervalXYZCT.min(4 ) );
		recorder.addOption( "maxX", intervalXYZCT.max(0 ) );
		recorder.addOption( "maxY", intervalXYZCT.max(1 ) );
		recorder.addOption( "maxZ", intervalXYZCT.max(2 ) );
		recorder.addOption( "maxC", intervalXYZCT.max(3 ) );
		recorder.addOption( "maxT", intervalXYZCT.max(4 ) );

		recorder.record();
	}
}

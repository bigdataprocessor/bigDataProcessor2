package de.embl.cba.bdp2.transform;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.AbstractProcessingDialog;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.Arrays;

public class TransformDialog< T extends RealType< T > & NativeType< T > > extends AbstractProcessingDialog< T >
{
	private static String affine = "1,0,0,0,0,1,0,0,0,0,1,0";
	private static String interpolation = TransformCommand.N_LINEAR;

	public TransformDialog( final BdvImageViewer< T > viewer )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
		showDialog();
	}

	private void showDialog()
	{
		final GenericDialog genericDialog = new GenericDialog( "Calibration" );
		genericDialog.addStringField( TransformCommand.AFFINE_LABEL, affine, 30 );
		genericDialog.addChoice( "Interpolation", new String[]{ TransformCommand.N_LINEAR }, interpolation );
		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;
		affine = genericDialog.getNextString();
		interpolation = genericDialog.getNextChoice();
		outputImage = BigDataProcessor2.transform( inputImage, TransformCommand.getAffineTransform3D( affine ), TransformCommand.getInterpolator( interpolation ) );

		viewer.replaceImage( outputImage, false, false );
		recordMacro();
	}

	@Override
	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( TransformCommand.COMMAND_FULL_NAME, inputImage, outputImage );
		recorder.addOption( TransformCommand.AFFINE_STRING_PARAMETER, affine );
		recorder.addOption( TransformCommand.INTERPOLATION_PARAMETER, interpolation );
		recorder.record();
	}
}

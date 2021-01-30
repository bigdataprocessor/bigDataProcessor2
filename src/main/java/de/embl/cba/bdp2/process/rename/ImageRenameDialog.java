package de.embl.cba.bdp2.process.rename;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static de.embl.cba.bdp2.process.rename.ImageRenameCommand.CHANNEL_NAMES_PARAMETER;

public class ImageRenameDialog < R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final ImageViewer< R > viewer;
	private String[] channelNames;

	public ImageRenameDialog( final ImageViewer< R > viewer  )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
	}

	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( ImageRenameCommand.COMMAND_FULL_NAME, inputImage );
		recorder.addCommandParameter( CHANNEL_NAMES_PARAMETER, String.join( ",", channelNames ) );

		// Image< R > rename( Image< R > image, String name )
		recorder.setAPIFunctionName( "rename" );
		recorder.addAPIFunctionPrequelComment(  ImageRenameCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( ScriptRecorder.quote( inputImage.getName() ) );
		recorder.addAPIFunctionParameter( inputImage.getChannelNames() );
		recorder.record();
	}

	protected void showDialog()
	{
		final GenericDialog gd = new GenericDialog( "Rename Image" );

		final int length = inputImage.getName().length();
		gd.addStringField( "Image", inputImage.getName(), 2 * length );

		channelNames = inputImage.getChannelNames();
		for ( int c = 0; c < channelNames.length; c++ )
		{
			gd.addStringField( "Channel " + c, channelNames[ c ], 2 * length  );
		}

		gd.showDialog();

		if( gd.wasCanceled() ) return;

		String name = gd.getNextString();

		for ( int c = 0; c < channelNames.length; c++ )
		{
			channelNames[ c ] = gd.getNextString();
		}

		BigDataProcessor2.rename( inputImage, name, channelNames );

		viewer.replaceImage( inputImage, false, true );

		recordMacro();
	}
}

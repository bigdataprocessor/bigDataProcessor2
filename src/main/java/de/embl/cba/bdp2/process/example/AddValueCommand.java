package de.embl.cba.bdp2.process.example;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.process.bin.BinCommand;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = AbstractImageProcessingCommand.class, name = AddValueCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + AddValueCommand.COMMAND_FULL_NAME )
public class AddValueCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Add Value...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    // Note: one may add a callback here to
    // give a live preview of the processed image
    @Parameter(label = "Value")
    public double value;

    // method that is executed upon pressing OK
    public void run()
    {
        outputImage = process( inputImage, value );
        handleOutputImage( false, true );
    }

    /**
     * Static method that does the processing, with signature:
     * outputImage = process( inputImage, parameters... );
     * One does not strictly have to implement this static method with this signature, but we recommend it such that the code can also be easily used via its Java API.
     */
    public static < R extends RealType< R > & NativeType< R > > Image< R > process( Image< R > image, final double value  )
    {
        // Make a copy of the image (no pixel data is copied here)
        Image< R > outputImage = new Image<>( image );

        // Get the 5D rai (x,y,z,c,t) containing the pixel data
        final RandomAccessibleInterval< R > rai = image.getRai();

        // Lazily add the value to each pixel in the rai
        // Note: There are no checks in this implementation whether the
        // result can be represented in the current data type R
        final RandomAccessibleInterval< R > convert = Converters.convert( rai, ( i, o ) -> o.setReal( i.getRealDouble() + value ), Util.getTypeFromInterval( rai ) );

        // Set this rai as the pixel source of the output image
        outputImage.setRai( convert );

        return outputImage;
    }

    /**
     * This is the method that will be called from the BDP2 menu.
     *
     * @param viewer
     *                 The active BigDataViewer window.
     *                 The corresponding active image can be accessed with viewer.getImage()
     */
    @Override
    public void showDialog( ImageViewer< R > viewer )
    {
        // Show the UI of this Command
        Services.getCommandService().run( AddValueCommand.class, true );

        // For simplicity, the viewer input parameter is not used in this example.
        // It may be used to build a more sophisticated and interactive UI that automatically operates on and updates the active image. See, e.g. de.embl.cba.bdp2.process.bin.BinCommand
    }
}

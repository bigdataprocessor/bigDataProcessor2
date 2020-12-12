package de.embl.cba.bdp2.process.bin;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import static de.embl.cba.bdp2.dialog.DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT;

@Plugin(type = AbstractImageProcessingCommand.class, name = BinCommand.COMMAND_NAME,
        menuPath = BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + BinCommand.COMMAND_FULL_NAME)
public class BinCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Bin...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Bin width X [pixels]", min = "1")
    int binWidthXPixels = 1;

    @Parameter(label = "Bin width Y [pixels]", min = "1")
    int binWidthYPixels = 1;

    @Parameter(label = "Bin width Z [pixels]", min = "1")
    int binWidthZPixels = 1;

    @Override
    public void run()
    {
        process();
        handleOutputImage( true, true );
    }

    private void process()
    {
        outputImage = BigDataProcessor2.bin( inputImage, new long[]{ binWidthXPixels, binWidthYPixels, binWidthZPixels, 1, 1 } );
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new BinDialog<>( imageViewer ).showDialog();
    }
}

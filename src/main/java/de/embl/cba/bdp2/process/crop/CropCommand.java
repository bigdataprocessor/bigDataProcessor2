package de.embl.cba.bdp2.process.crop;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = AbstractImageProcessingCommand.class, name = CropCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PROCESS_PATH + CropCommand.COMMAND_FULL_NAME )
public class CropCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Crop...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    long minX = 0;

    @Parameter(label = "Min Y [pixel]", min = "0")
    long minY = 0;

    @Parameter(label = "Min Z [pixel]", min = "0")
    long minZ = 0;

    @Parameter(label = "Min C [channel]", min = "0")
    long minC = 0;

    @Parameter(label = "Min T [frame]", min = "0")
    long minT = 0;
    
    @Parameter(label = "Max X [pixel]", min = "0")
    long maxX = 100;

    @Parameter(label = "Max Y [pixel]", min = "0")
    long maxY = 100;

    @Parameter(label = "Max Z [pixel]", min = "0")
    long maxZ = 100;

    @Parameter(label = "Max C [channel]", min = "0")
    long maxC = 0;

    @Parameter(label = "Max T [frame]", min = "0")
    long maxT = 0;

    @Override
    public void run()
    {
        process();
        handleOutputImage( false, false );
        ImageService.imageNameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        final FinalInterval minMax = Intervals.createMinMax(
                minX, minY, minZ, minC, minT,
                maxX, maxY, maxZ, maxC, maxT );

        outputImage = BigDataProcessor2.crop( inputImage, minMax );
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new CropDialog<>( imageViewer ).showDialog();
    }
}

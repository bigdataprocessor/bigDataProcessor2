package de.embl.cba.bdp2.convert;

import de.embl.cba.bdp2.scijava.command.AbstractProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Process>" + UnsignedByteTypeConversionCommand.COMMAND_NAME )
public class UnsignedByteTypeConversionCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand implements Command
{
    public static final String COMMAND_NAME = "BDP2_ConvertToUnsignedByte...";
    @Parameter(label = "Map to 0", min = "0")
    int mapTo0 = 0;

    @Parameter(label = "Map to 255", min = "0")
    int mapTo255 = 65535;

    @Override
    public void run()
    {
        process();
        handleOutputImage( true, true );
    }

    private void process()
    {
        final UnsignedByteTypeConverter converter = new UnsignedByteTypeConverter<>( inputImage, mapTo0, mapTo255 );

        outputImage = converter.getConvertedImage();
    }
}

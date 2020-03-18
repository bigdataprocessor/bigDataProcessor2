package de.embl.cba.bdp2.shift;

import de.embl.cba.bdp2.scijava.command.AbstractProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Process>BDP2_ChromaticShift...")
public class ChromaticShiftCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand implements Command
{
    @Parameter(label = "Shifts X_C0,Y_C0,Z_C0;X_C1,Y_C1,Z_C1;... [pixels]")
    String shifts = "0,0,0;0,0,0";

    @Override
    public void run()
    {
        process();
        showOutputImage();
        ImageService.nameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        final List< long[] > longs = stringToLongs( shifts );

        final ChannelShifter< R > shifter = new ChannelShifter< R >( inputImage.getRai() );
        outputImage = inputImage.newImage( shifter.getShiftedRai( longs ) );
        outputImage.setName( outputImageName );
    }

    public static String longsToString( ArrayList< long[] > shifts )
    {
        final String collect = shifts.stream().map( t ->
                Arrays.stream( t ).mapToObj( x -> String.valueOf( x ) )
                        .collect( Collectors.joining( "," ) ) )
                        .collect( Collectors.joining( ";" ) );

        return collect;
    }

    public static List< long[] > stringToLongs( String string )
    {
        final List< long[] > shifts = Arrays.stream( string.split( ";" ) )
                .map( t -> Arrays.stream( t.split( "," ) )
                        .mapToLong( Long::parseLong )
                        .toArray()
                ).collect( Collectors.toList() );

        return shifts;
    }
}

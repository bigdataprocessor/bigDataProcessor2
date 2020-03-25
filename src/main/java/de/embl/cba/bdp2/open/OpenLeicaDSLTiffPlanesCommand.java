package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.utils.Utils;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.io.File;


/**
 * TODO: How to add a HELP button for the regular expression without screwing up the macro recording?
 *
 *
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Open>" + OpenLeicaTiffPlanesCommand.COMMAND_NAME )
public class OpenLeicaTiffPlanesCommand< R extends RealType< R > & NativeType< R > > implements Command {

    public static final String COMMAND_NAME = Utils.COMMAND_PREFIX + "Open Leica DSL Tiff Planes...";
    @Parameter(label = "Image data directory", style = "directory")
    File directory;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            DebugTools.setRootLevel( "OFF" ); // Bio-Formats

            final Image< R > image =
                    BigDataProcessor2.openImage(
                            directory.toString(),
                            FileInfos.LEICA_LIGHT_SHEET_TIFF,
                            ".*.tif" );

            BigDataProcessor2.showImage( image, true );
        });
    }
}

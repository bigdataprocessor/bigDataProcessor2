package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
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
@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Open>BDP2_OpenLuxendoChannelsFromFolders...")
public class OpenLuxendoChannelsFromFoldersCommand< R extends RealType< R > & NativeType< R > > implements Command {

    @Parameter(label = "Image data directory (must contain channel sub-folders)", style = "directory")
    File directory;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            DebugTools.setRootLevel( "OFF" ); // Bio-Formats

            final Image< R > image =
                    BigDataProcessor2.openImage(
                            directory.toString(),
                            FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                            FileInfos.PATTERN_LUXENDO,
                            "Data");

            BigDataProcessor2.showImage( image, true );
        });
    }
}

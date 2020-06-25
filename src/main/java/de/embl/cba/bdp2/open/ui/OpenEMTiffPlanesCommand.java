package de.embl.cba.bdp2.open.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Command;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP_PREFIX;

@Plugin(type = Command.class, menuPath = BigDataProcessor2Command.BIGDATAPROCESSOR2_PLUGINS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenEMTiffPlanesCommand.COMMAND_FULL_NAME )
public class OpenEMTiffPlanesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open EM Tiff Planes...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP_PREFIX + COMMAND_NAME;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            outputImage = BigDataProcessor2.openImage(
                            directory.toString(),
                            NamingSchemes.TIFF_SLICES,
                            ".*.tif" );

            handleOutputImage( true, false );
        });
    }
}

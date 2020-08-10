package de.embl.cba.bdp2.open.ui;

import de.embl.cba.bdp2.dialog.HelpWindow;
import de.embl.cba.bdp2.dialog.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenCustomHelpCommand.COMMAND_FULL_NAME )
public class OpenCustomHelpCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_NAME = "Open Custom Help...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    public void run()
    {

        SwingUtilities.invokeLater( () -> {
            final HelpWindow helpWindow = new HelpWindow( AbstractOpenCommand.class.getResource( "/RegExpHelp.html" ) );
            helpWindow.setVisible( true );
        } );
    }
}

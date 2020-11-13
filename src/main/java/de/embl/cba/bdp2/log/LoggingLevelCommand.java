package de.embl.cba.bdp2.log;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + LoggingLevelCommand.COMMAND_FULL_NAME )
public class LoggingLevelCommand implements Command
{
    public static final String COMMAND_NAME = "Set Logging Level...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    /**
     * 	public enum Level
     *    {
     * 		Normal,
     * 		Debug,
     * 		Benchmark
     *    }
     */

    @Parameter(label = "Logging level", choices = {"Normal","Debug","Benchmark"})
    String level = Logger.getLevel().toString();

    public void run()
    {
        Logger.setLevel( Logger.Level.valueOf( level ) );
    }
}
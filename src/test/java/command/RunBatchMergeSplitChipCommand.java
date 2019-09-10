package command;

import de.embl.cba.bdp2.command.BatchMergeSplitChipCommand;
import de.embl.cba.bdp2.command.BigDataProcessorCommand;
import net.imagej.ImageJ;

public class RunBatchMergeSplitChipCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run( BatchMergeSplitChipCommand.class, true );
	}
}
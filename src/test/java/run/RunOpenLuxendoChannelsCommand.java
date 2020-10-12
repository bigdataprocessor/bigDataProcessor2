package run;

import de.embl.cba.bdp2.open.luxendo.OpenLuxendoChannelsCommand;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class RunOpenLuxendoChannelsCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		new Recorder();

		ij.command().run( OpenLuxendoChannelsCommand.class, true );
	}
}
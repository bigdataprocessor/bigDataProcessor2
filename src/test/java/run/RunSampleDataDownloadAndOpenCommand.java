package run;

import de.embl.cba.bdp2.open.samples.DownloadAndOpenSampleDataCommand;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class RunSampleDataDownloadAndOpenCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		new Recorder();

		ij.command().run( DownloadAndOpenSampleDataCommand.class, true );
	}
}
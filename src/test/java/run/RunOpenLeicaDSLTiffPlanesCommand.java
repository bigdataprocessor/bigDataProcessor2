package run;

import de.embl.cba.bdp2.open.fileseries.OpenLeicaDSLTIFFPlaneSeriesCommand;
import de.embl.cba.bdp2.scijava.Services;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class RunOpenLeicaDSLTiffPlanesCommand
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		new Recorder();

		Services.setCommandService( ij.command() );

		ij.command().run( OpenLeicaDSLTIFFPlaneSeriesCommand.class, true );
	}
}
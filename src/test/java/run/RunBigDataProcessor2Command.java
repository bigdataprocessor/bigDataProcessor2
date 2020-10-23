package run;

import de.embl.cba.bdp2.BigDataProcessor2Command;
import de.embl.cba.bdp2.scijava.Services;
import ij.IJ;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;

public class RunBigDataProcessor2Command
{
	public static void main ( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		//Prefs prefs = new Prefs();
		Prefs.set( "recorder.mode", "JavaScript" );
		new Recorder();

		//ij.command().run( BigDataProcessor2Command.class, true );
	}
}
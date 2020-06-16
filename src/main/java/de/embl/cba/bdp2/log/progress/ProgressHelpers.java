package de.embl.cba.bdp2.log.progress;

import de.embl.cba.bdp2.log.Logger;
import ij.IJ;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressHelpers
{
	public static void logProgress( long total, AtomicInteger counter, final long startTimeMillis )
	{
		logProgress( total, counter, startTimeMillis, "" );
	}

	public static void logProgress( long total, AtomicInteger counter, final long startTimeMillis, String msg )
	{
		Double secondsSpent = (1.0 * System.currentTimeMillis() - startTimeMillis ) / (1000.0);
		Double secondsPerTask = counter.get() > 0 ? secondsSpent / counter.get() : Double.NaN ;
		Double secondsLeft = (total - counter.get()) * secondsPerTask;

		String unit = "s";
		double divisor = 1;
		if ( secondsSpent > 3 * 60 )
		{
			unit = "min";
			divisor = 60;
		}

		Logger.progress( msg,
				"" + counter.get() + "/" + total
						+ "; time ( spent, left, task ) [ " + unit + " ]: "
						+ ( int ) ( secondsSpent / divisor )
						+ ", " + ( int ) ( secondsLeft / divisor )
						+ ", " + String.format("%.3g", secondsPerTask / divisor)
						+ "; memory: "
						+ IJ.freeMemory() );
	}
}

package de.embl.cba.bdp2.log;

import de.embl.cba.bdp2.BigDataProcessor2UserInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PerformanceMonitor
{
	public static final int MEGA = 1000000;
	private List< Double > readPerformances;
	private List< Double > copyPerformances;

	public PerformanceMonitor()
	{
		readPerformances = Collections.synchronizedList(new ArrayList<>( ));
		copyPerformances = Collections.synchronizedList(new ArrayList<>( ));
	}

	public synchronized void addReadPerformance( long numBytes, long timeMillis )
	{
		final double speed = toMBytePerSecond( numBytes, timeMillis );
		synchronized ( readPerformances )
		{
			readPerformances.add( speed );
		}

		BigDataProcessor2UserInterface.setReadPerformanceInformation( speed, getMedianReadPerformance() );
	}

	private double toMBitPerSecond( long bytes, long millis )
	{
		return toMBit( bytes ) / toSeconds( millis );
	}

	private double toMBytePerSecond( long bytes, long millis )
	{
		return toMByte( bytes ) / toSeconds( millis );
	}

	private double toSeconds( long timeMillis )
	{
		return timeMillis / 1000.0;
	}

	private double toMBit( long bytes )
	{
		return 1.0 * bytes * 8 / MEGA;
	}

	private double toMByte( long bytes )
	{
		return 1.0 * bytes / MEGA;
	}

	public double getMedianReadPerformance()
	{
		if ( readPerformances.size() == 0 ) return 0;

		synchronized ( readPerformances )
		{
			final double median = readPerformances.stream().mapToDouble( x -> x ).sorted().skip(readPerformances.size()/2).findFirst().getAsDouble();
			return median;
		}
	}

	public void addCopyPerformance( int numBytes, long timeMillis )
	{
		copyPerformances.add( toMBitPerSecond( numBytes, timeMillis ) );
	}

	public double getAverageCopyPerformance()
	{
		return readPerformances.stream().mapToDouble( x -> x ).average().getAsDouble();
	}
}

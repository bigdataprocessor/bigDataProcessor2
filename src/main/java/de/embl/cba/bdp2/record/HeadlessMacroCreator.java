package de.embl.cba.bdp2.record;

import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.quit.QuitCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HeadlessMacroCreator
{
	private final File macroFile;
	private ArrayList< String > lines;

	public HeadlessMacroCreator( File macroFile )
	{
		this.macroFile = macroFile;
		readLines( macroFile );
	}

	private void readLines( File macroFile )
	{
		try
		{
			BufferedReader bufferedReader = new BufferedReader( new FileReader( macroFile ) );

			lines = new ArrayList<>();
			String line;
			while ( ( line = bufferedReader.readLine() ) != null )
			{
				if ( ! line.endsWith( ";" ) )
					line = line + ";";
				lines.add( line );
			}
			
		} catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public String createHeadlessExecutableMacroString()
	{
		ArrayList< String > headlessCommands = new ArrayList<>();

		headlessCommands.add( "run(\"BDP2 Set Logging Level...\", \"level=Benchmark\");" );

		for ( String command : lines )
		{
			if ( command.contains( "BigDataProcessor2") ) continue;
			if ( command.equals( "" ) ) continue;

			command = command.replace( AbstractOpenFileSeriesCommand.SHOW_IN_NEW_VIEWER, AbstractOpenFileSeriesCommand.DO_NOT_SHOW );
			command = command.replace( AbstractOpenFileSeriesCommand.SHOW_IN_CURRENT_VIEWER, AbstractOpenFileSeriesCommand.DO_NOT_SHOW );
			headlessCommands.add( command );
		}

		headlessCommands.add( "run(\"" + QuitCommand.COMMAND_FULL_NAME +"\");" );

		String join = headlessCommands.stream().collect( Collectors.joining( " " ) );

		return join;
	}

	public static void main( String[] args )
	{
		HeadlessMacroCreator macroCreator = new HeadlessMacroCreator( new File( "/Users/tischer/Desktop/tmp2/SaveBDP2.ijm" ) );
		String headlessString = macroCreator.createHeadlessExecutableMacroString();
		System.out.println(
				"/Users/tischer/Desktop/Fiji-bdp2.app/Contents/MacOS/ImageJ-macosx --headless --mem=10000M -eval " +
						"'" + headlessString + "'" );
		// /Users/tischer/Desktop/Fiji-imflow.app/Contents/MacOS/ImageJ-macosx --headless -eval
	}

}

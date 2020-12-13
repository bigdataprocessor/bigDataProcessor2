package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.log.Logger;
import ij.io.BitBuffer;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class TiffDecompressor
{
	public static final int COMPRESSION_UNKNOWN = 0;
	public static final int COMPRESSION_NONE = 1;
	public static final int LZW = 2;
	public static final int LZW_WITH_DIFFERENCING = 3;
	public static final int ZIP = 6;
	public static final int PACK_BITS = 5;
	public static final int JPEG = 4;
	private static final int CLEAR_CODE = 256;
	private static final int EOI_CODE = 257;


	/**
	 *
	 * TODO: this could be faster, because we know how many bytes it will be in the end
	 *
	 *
	 * @param bytes
	 * @return
	 */
	public static byte[] decompressZIP( byte[] bytes )
	{
		/** TIFF Adobe ZIP support contributed by Jason Newton. */
		ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
		byte[] tmpBuffer = new byte[ 1024 ];
		Inflater decompressor = new Inflater();

		decompressor.setInput( bytes );
		try
		{
			while ( !decompressor.finished() )
			{
				int rlen = decompressor.inflate( tmpBuffer );
				imageBuffer.write( tmpBuffer, 0, rlen );
			}
		} catch ( DataFormatException e )
		{
			Logger.error( e.toString() );
		}
		decompressor.end();

		bytes = imageBuffer.toByteArray();
		return bytes;
	}

	public static byte[] decompressLZW( byte[] input, int numOutputBytes )
	{
		long startTimeGlob = System.nanoTime();

		byte[][] symbolTable = new byte[ 16384 ][ 1 ]; // enlarged to be compatible with larger images

		if ( input == null || input.length == 0 )
			return input;

		int bitsToRead = 9;
		int nextSymbol = 258;
		int code;
		int symbolLength, symbolLengthMax = 0;
		int oldCode = -1;
		//ByteVector out = new ByteVector(8192);
		byte[] out = new byte[ numOutputBytes ];
		int iOut = 0, i;
		int k = 0;
		BitBuffer bb = new BitBuffer( input );

		byte[] byteBuffer1 = new byte[ 16 ];
		byte[] byteBuffer2 = new byte[ 16 ];

		while ( iOut < numOutputBytes )
		{
			//startTime2 = System.nanoTime();
			code = bb.getBits( bitsToRead );
			//totalTime2 += (System.nanoTime() - startTime2);
			if ( code == EOI_CODE || code == -1 )
				break;
			if ( code == CLEAR_CODE )
			{
				//startTime4 = System.nanoTime();
				// initialize symbol jTableSpots
				for ( i = 0; i < 256; i++ )
					symbolTable[ i ][ 0 ] = ( byte ) i;
				nextSymbol = 258;
				bitsToRead = 9;
				code = bb.getBits( bitsToRead );
				if ( code == EOI_CODE || code == -1 )
					break;
				//out.add(symbolTable[code]);
				System.arraycopy( symbolTable[ code ], 0, out, iOut, symbolTable[ code ].length );
				iOut += symbolTable[ code ].length;
				oldCode = code;
				//totalTime4 += (System.nanoTime() - startTime4);

			} else
			{
				if ( code < nextSymbol )
				{
					//startTime6 = System.nanoTime();
					// code is in jTableSpots
					//startTime5 = System.nanoTime();
					//out.add(symbolTable[code]);
					symbolLength = symbolTable[ code ].length;
					System.arraycopy( symbolTable[ code ], 0, out, iOut, symbolLength );
					iOut += symbolLength;
					//totalTime5 += (System.nanoTime() - startTime5);
					// add string to jTableSpots

					//ByteVector symbol = new ByteVector(byteBuffer1);
					//symbol.add(symbolTable[oldCode]);
					//symbol.add(symbolTable[code][0]);
					int lengthOld = symbolTable[ oldCode ].length;

					//byte[] newSymbol = new byte[lengthOld+1];
					symbolTable[ nextSymbol ] = new byte[ lengthOld + 1 ];
					System.arraycopy( symbolTable[ oldCode ], 0, symbolTable[ nextSymbol ], 0, lengthOld );
					symbolTable[ nextSymbol ][ lengthOld ] = symbolTable[ code ][ 0 ];
					//symbolTable[nextSymbol] = newSymbol;

					oldCode = code;
					nextSymbol++;
					//totalTime6 += (System.nanoTime() - startTime6);

				}
				else
				{
					//startTime3 = System.nanoTime();
					// out of jTableSpots
					ByteVector symbol = new ByteVector( byteBuffer2 );
					symbol.add( symbolTable[ oldCode ] );
					symbol.add( symbolTable[ oldCode ][ 0 ] );
					byte[] outString = symbol.toByteArray();
					//out.add(outString);
					System.arraycopy( outString, 0, out, iOut, outString.length );
					iOut += outString.length;
					symbolTable[ nextSymbol ] = outString; //**
					oldCode = code;
					nextSymbol++;
					//totalTime3 += (System.nanoTime() - startTime3);

				}
				if ( nextSymbol == 511 )
				{
					bitsToRead = 10;
				}
				if ( nextSymbol == 1023 )
				{
					bitsToRead = 11;
				}
				if ( nextSymbol == 2047 )
				{
					bitsToRead = 12;
				}
				if ( nextSymbol == 4095 )
				{
					bitsToRead = 13;
				}
				if ( nextSymbol == 8191 )
				{
					bitsToRead = 14;
				}
				if ( nextSymbol == 16383 )
				{
					Logger.error( "Symbol table of LZW uncompression became too large." +
							"\nThe next symbol would have been: " + nextSymbol +
							"\nPlease contact tischitischer@gmail.com" );
					return null;
				}
				;
			}

		}

		return out;
	}

	/**
	 * Based on the Bio-Formats PackbitsCodec written by Melissa Linkert.
	 */
	public byte[] packBitsUncompress( byte[] input, int expected )
	{
		if ( expected == 0 ) expected = Integer.MAX_VALUE;
		ByteVector output = new ByteVector( 1024 );
		int index = 0;
		while ( output.size() < expected && index < input.length )
		{
			byte n = input[ index++ ];
			if ( n >= 0 )
			{ // 0 <= n <= 127
				byte[] b = new byte[ n + 1 ];
				for ( int i = 0; i < n + 1; i++ )
					b[ i ] = input[ index++ ];
				output.add( b );
				b = null;
			} else if ( n != -128 )
			{ // -127 <= n <= -1
				int len = -n + 1;
				byte inp = input[ index++ ];
				for ( int i = 0; i < len; i++ ) output.add( inp );
			}
		}
		return output.toByteArray();
	}

	/**
	 * Based on the Bio-Formats PackbitsCodec written by Melissa Linkert.
	 */
	public static byte[] packBitsUncompressFast( byte[] input, int expected )
	{
		if ( expected == 0 ) expected = Integer.MAX_VALUE;
		int inputIndex = 0;
		int outputIndex = 0;

		final byte[] output = new byte[ expected ];
		while ( inputIndex < expected && inputIndex < input.length )
		{
			byte n = input[ inputIndex++ ];
			if ( n >= 0 )
			{ // 0 <= n <= 127
				for ( int i = 0; i < n + 1; i++ )
					output[ outputIndex++ ] = input[ inputIndex++ ];
			} else if ( n != -128 )
			{ // -127 <= n <= -1
				int len = -n + 1;
				byte inp = input[ inputIndex++ ];
				for ( int i = 0; i < len; i++ ) output[ outputIndex++ ] = inp;
			}
		}
		return output;
	}

}

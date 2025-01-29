/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.open.fileseries;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.open.fileseries.hdf5.HDF5Helper;
import de.embl.cba.imaris.ImarisUtils;
import de.embl.cba.util.OSUtils;
import ij.io.FileInfo;
import loci.common.DebugTools;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.io.IOException;

public class FileInfos
{
    public static final int PROGRESS_UPDATE_MILLISECONDS = 100;

    // TODO: below must be in synch with DimensionOrder
	public static final AxisType[] AXES_ORDER = { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME};
    public static final String[] HDF5_DATASET_NAMES = new String[] {
            "None", "Data", "Data111", "Data222", "Data444", // Luxendo
			ImarisUtils.RESOLUTION_LEVEL +"0/Data",
			ImarisUtils.RESOLUTION_LEVEL +"1/Data",
			ImarisUtils.RESOLUTION_LEVEL +"2/Data",
			ImarisUtils.RESOLUTION_LEVEL +"3/Data",
			"ITKImage/0/VoxelData"
    };
    public BDP2FileInfo[][][] ctzFileInfos;
    public long[] dimensions;
    public boolean containsHDF5DatasetSingletonDimension = false;
    private String namingScheme;
    public int bitDepth;
    public int nC;
    public int nT;
    public int nX;
    public int nY;
    public int nZ;
    public String voxelUnit;
    public double[] voxelSize;
    public FileSeriesFileType fileType;
    public String h5DataSetName;
    public String[][][] ctzFilePaths;
    public String directory;
    public double max_pixel_val;
    public double min_pixel_val;
    public int compression;
    public int numTIFFStrips;
    public String[] channelNames;
    public String[] paths;
    private boolean recursive;

    public FileInfos(
            String directory,
            String regExp )
    {
        this( directory, regExp, null, null, null );
    }

    public FileInfos(
            String directory,
            String regExp,
            String h5DataSetName )
    {
        this( directory, regExp, h5DataSetName, null, null );
    }

    public FileInfos(
            String directory,
            String regExp,
            String h5DataSetPath,
            String[] channelSubset )
    {
        this( directory, regExp, h5DataSetPath, channelSubset, null );
    }

    public FileInfos(
            String directory,
            String regExp,
            String[] channelSubset )
    {
        this( directory, regExp, null, channelSubset, null );
    }

    public FileInfos(
            String directory,
            String namingScheme,
            String h5DataSetPath,
            String[] channelSubset,
            String[] paths
    )
    {
        this.paths = paths;
        fetchFileInfos( directory, namingScheme, h5DataSetPath, channelSubset );
    }

    private void fetchFileInfos( String aDirectory, String regExp, String h5DataSetName, String[] channelSubset )
    {
        this.namingScheme = regExp;
        this.directory = FileInfosHelper.ensureDirectoryEndsWithFileSeparator( aDirectory );
        this.h5DataSetName = h5DataSetName;

        DebugTools.setRootLevel( "OFF" ); // Bio-Formats

        // TODO: do we need the recursive option still??
        if( namingScheme.contains( NamingSchemes.NONRECURSIVE ) )
        {
            recursive = false;
            namingScheme = namingScheme.replace( NamingSchemes.NONRECURSIVE, "" );
        }
        else
        {
            recursive = true;
            namingScheme = namingScheme;
        }

        if ( OSUtils.isWindows() )
        {
            // replace / by \\
            // the namingSchemes can contain folders, because
            // some file formats like Luxendo spread the information
            // which channel an image is across file and folder names.
            // Internally, we currently represent the folder separator by
            // "/" (see NamingSchemes class).
            // To make this work with Windows we thus have to replace.
            // Note: Maybe an alternative could be to leave it "/" and then
            // switch it in the folders/files that we want to match.
            // Maybe that would be easier?
            namingScheme = namingScheme.replaceAll("/", "\\\\\\\\" );
        }

        Logger.info( "Directory: " + directory );
        Logger.info( "Regular expression: " +  namingScheme );

        if ( this.paths == null )
            this.paths = FileInfosHelper.fetchFiles( directory, namingScheme );

        FileInfosHelper.setFileInfos5D( this, namingScheme, channelSubset );

        Logger.info( this.toString() );
    }


    @Override
    public String toString()
    {
        String info = "";
        info += "Folder: " + directory + "\n";
        info += "FileType: " + fileType + "\n";
        info += "BitDepth: " + bitDepth + "\n";

        if ( fileType.toString().toLowerCase().contains( "tif" ) )
        {
            info += "TIFF Compression: " + getCompressionString() + "\n";
            info += "TIFF Strips: " + numTIFFStrips + "\n";
        }
        info += "nX: " + nX + "\n";
        info += "nY: " + nY + "\n";
        info += "nZ: " + nZ + "\n";
        info += "nC: " + nC + "\n";
        info += "nT: " + nT + "\n";
        info += "voxelUnit: " + voxelUnit + "\n";
        info += "voxelSizeX: " + voxelSize[0] + "\n";
        info += "voxelSizeY: " + voxelSize[1] + "\n";
        info += "voxelSizeZ: " + voxelSize[2] + "\n";
        info += "GB: " + getSizeInGB() + "\n";
        return info;
    }

    private String getCompressionString()
    {
        String compressionString;
        switch ( compression )
        {
            case FileInfo.ZIP: compressionString = "ZIP"; break;
            case FileInfo.LZW: compressionString = "LZW"; break;

            default: compressionString = "None";
        }
        return compressionString;
    }

    private double getSizeInGB()
    {
        long bytes = (long) bitDepth / 8 * nX * nY * nZ * nT * nC;
        final double sizeGB = ( double ) bytes / ( 1000_000_000L );
        return sizeGB;
    }

    public long[] getDimensions() {
        return dimensions;
    }

    public NativeType getType() {
        NativeType type;
        try {
            if (bitDepth == Byte.SIZE) {
                type = new UnsignedByteType();
            } else if (bitDepth == Short.SIZE) {
                type = new UnsignedShortType();
            } else if (bitDepth == Float.SIZE) {
                type = new FloatType();
            } else {
                throw new TypeNotPresentException("Data Type Not Found", new Throwable());
            }
        }catch (TypeNotPresentException typeException){
            throw typeException;
        }
    return  type;
    }

    public BDP2FileInfo[] getVolumeFileInfos( int channel, int time )
    {
        int z = 0;

        if ( FileSeriesFileType.is3D( fileType ) )
        {
            setInfosFromFile( channel, time, z );
        }
        else if ( FileSeriesFileType.is2D( fileType ) )
        {
            int nZ = ctzFilePaths[channel][time].length;
            for (; z < nZ; ++z)
            {
                setInfosFromFile(channel, time, z );
            }
        }
        else
        {
            throw new UnsupportedOperationException( "File type not supported " + fileType.toString() );
        }

        return ctzFileInfos[channel][time];
    }

    private void setInfosFromFile( final int c, final int t, final int z )
    {
        BDP2FileInfo[] info = null;

        File file = new File( ctzFilePaths[c][t][z] );
        if ( file.exists() )
        {
            if ( fileType.equals( FileSeriesFileType.TIFF_STACKS ) )
            {
                loadMetadataFromTIFFStack( c, t, info, file );
            }
            else if ( fileType.equals( FileSeriesFileType.LUXENDO ) || fileType.equals( FileSeriesFileType.HDF5_VOLUMES ) )
            {
                loadMetadataFromHDF5Stack( c, t, file );
            }
            else if ( fileType.equals( FileSeriesFileType.TIFF_PLANES) )
            {
                loadMetadataFromTIFFPlane( c, t, z );
            }
        }
        else
        {
            Logger.error( "File does not exist [ c, t, z ] : " + c + ", " + t + ", " + z+
                            "\npath:" + file.getAbsolutePath()  +
                            "\ndirectory: " + directory +
                            "\nfile: " + ctzFilePaths[c][t][z] );
            throw new UnsupportedOperationException( "File does not exist " + file.getAbsolutePath() );
        }

    }

    private void loadMetadataFromTIFFPlane( int c, int t, int z )
    {
        FastTIFFDecoder ftd;
        ftd = new FastTIFFDecoder( ctzFilePaths[ c ][ t ][ z ] );
        try{
            ctzFileInfos[ c ][ t ][ z ] = ftd.getTIFFInfo()[0];
        }
        catch ( IOException e ){// TODO : Handle exceptions properly --ashis
            System.out.print( e.toString() );
        }
        ctzFileInfos[ c ][ t ][ z ].fileName = getName( c, t, z );
        ctzFileInfos[ c ][ t ][ z ].directory = getDirectory( c, t, z );
        ctzFileInfos[ c ][ t ][ z ].fileTypeString = fileType.toString();
    }

    private void loadMetadataFromHDF5Stack( int c, int t, File file )
    {
        BDP2FileInfo[] infoCT;
        int bytesPerPixel = 0;

        IHDF5Reader reader = HDF5Factory.openForReading( file.getAbsolutePath() );
        HDF5DataSetInformation dsInfo = reader.getDataSetInformation( h5DataSetName );
        //String dsTypeString = OpenerExtension.hdf5InfoToString(dsInfo);
        String dsTypeString = HDF5Helper.dsInfoToTypeString(dsInfo); //TODO: Check if OpenerExtension.hdf5InfoToString can be made public and called.

        if (dsTypeString.equals("int16") || dsTypeString.equals("uint16")){
            bytesPerPixel = 2;
        }
        else if (dsTypeString.equals("int8") || dsTypeString.equals("uint8")){
            bytesPerPixel = 1;
        }
        else{
            Logger.error( "Unsupported bit depth " + dsTypeString );
        }

        infoCT = new BDP2FileInfo[nZ];
        for ( int z2 = 0; z2 < nZ; z2++)
        {
            infoCT[z2] = new BDP2FileInfo();
            infoCT[z2].fileName = getName( c, t, z2 );
            infoCT[z2].directory = getDirectory( c, t, z2 );
            infoCT[z2].width = nX;
            infoCT[z2].height = nY;
            infoCT[z2].bytesPerPixel = bytesPerPixel;
            infoCT[z2].h5DataSet = h5DataSetName;
            infoCT[z2].fileTypeString = fileType.toString();
        }
        ctzFileInfos[ c ][ t ] = infoCT;
    }

    private void loadMetadataFromTIFFStack( int c, int t, BDP2FileInfo[] info, File file )
    {
        FastTIFFDecoder ftd;
        BDP2FileInfo[] infoCT;
        ftd = new FastTIFFDecoder( ctzFilePaths[ c ][ t ][0] );
        try {
            info = ftd.getTIFFInfo();
        }
        catch (Exception e) {
            Logger.error("Error parsing: " + file.getAbsolutePath() );
            Logger.warn("setInfoFromFile: " + e.toString());
        }

        if( info.length != nZ ) {// TODO : Handle exceptions properly --ashis
            Logger.error("Inconsistent number of z-planes in: " + file.getAbsolutePath());
        }

        // add missing information to first IFD
        info[0].fileName = getName( c, t, 0 );
        info[0].directory = getDirectory( c, t, 0 );
        info[0].fileTypeString = fileType.toString();

        infoCT = new BDP2FileInfo[nZ];
        for ( int z2 = 0; z2 < nZ; z2++ ) {
            infoCT[z2] = new BDP2FileInfo( info[0] ); // copyVolumeRAI constructor
            // adapt information related to where the data is stored in this plane
            infoCT[z2].offset = info[z2].offset;
            infoCT[z2].stripLengths = info[z2].stripLengths;
            infoCT[z2].stripOffsets = info[z2].stripOffsets;
            //infoCT[z].rowsPerStrip = info[z].rowsPerStrip; // only core for first IFD!
        }

        ctzFileInfos[ c ][ t ] = infoCT;
    }

    private String getName( int c, int t, int z )
    {
        return new File( ctzFilePaths[ c ][ t ][ z ] ).getName();
    }

    private String getDirectory( int c, int t, int z )
    {
        final String parent = new File( ctzFilePaths[ c ][ t ][ z ] ).getParent();

        if ( parent == null )
            return "";
        else
            return parent;
    }
}

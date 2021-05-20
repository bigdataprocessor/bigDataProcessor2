package de.embl.cba.bdp2.open.fileseries.hdf5;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import ij.gui.GenericDialog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HDF5Helper
{
    public static final String HDF5_PARSING_ERROR = "Error during hdf5 metadata extraction from ";

    public static void setMetadataFromHDF5(
            FileInfos fileInfos,
            String directory,
            String fileName)
    {
        final String filePath = directory + "/" + fileName;
        IHDF5Reader reader = HDF5Factory.openForReading( filePath );

        StringBuilder hdf5DataSetSB = new StringBuilder();
        if ( fileInfos.h5DataSetName != null && !fileInfos.h5DataSetName.isEmpty()
                && !fileInfos.h5DataSetName.trim().isEmpty() )
        {
            // TODO: improve this, try different names recursively
            hdf5DataSetSB = new StringBuilder( fileInfos.h5DataSetName );
            if ( !hdf5DataSetExists( reader, hdf5DataSetSB ) )
            {
                if ( !setHDF5DatasetViaUI( reader, hdf5DataSetSB ) )
                    throw new RuntimeException( HDF5_PARSING_ERROR + filePath );
            }
        } else
        {
            if ( !setHDF5DatasetViaUI( reader, hdf5DataSetSB ) )
                throw new RuntimeException( HDF5_PARSING_ERROR + filePath );
        }

        fileInfos.h5DataSetName = hdf5DataSetSB.toString();
        HDF5DataSetInformation dsInfo = reader.object().getDataSetInformation( "/" + fileInfos.h5DataSetName );

        if ( dsInfo.getDimensions().length == 3 )
        {
            fileInfos.nZ = ( int ) dsInfo.getDimensions()[ 0 ];
            fileInfos.nY = ( int ) dsInfo.getDimensions()[ 1 ];
            fileInfos.nX = ( int ) dsInfo.getDimensions()[ 2 ];
        }
        else if ( dsInfo.getDimensions().length == 2 )
        {
            fileInfos.nZ = 1;
            fileInfos.nY = ( int ) dsInfo.getDimensions()[ 0 ];
            fileInfos.nX = ( int ) dsInfo.getDimensions()[ 1 ];
        }
        else if ( dsInfo.getDimensions().length == 4 )
        {
            fileInfos.nZ = ( int ) dsInfo.getDimensions()[ 0 ];
            fileInfos.nY = ( int ) dsInfo.getDimensions()[ 1 ];
            fileInfos.nX = ( int ) dsInfo.getDimensions()[ 2 ];
            // we ignore the 4th dimension which could be an ilastik channel
            Logger.warn( "Found 4 dimensions in HDF5 dataset." );
            Logger.warn( "Ignoring the 4th dimension, because only 3D volumes are supported." );
            fileInfos.containsHDF5DatasetSingletonDimension = true;
        }

        fileInfos.bitDepth = assignHDF5TypeToImagePlusBitdepth(dsInfo);

       if ( ! setVoxelSizeFromLuxendoHDF5( reader, fileInfos ) );
       {
           fileInfos.voxelSize = new double[]{1,1,1};
           fileInfos.voxelUnit = "pixel";
       }
    }

    private static int assignHDF5TypeToImagePlusBitdepth(HDF5DataSetInformation dsInfo) {
        String type = dsInfoToTypeString(dsInfo);
        if (type.equals("uint8")) {
            return Byte.SIZE;
        } else if (type.equals("uint16") || type.equals("int16")) {
            return Short.SIZE;
        } else if (type.equals("float32") || type.equals("float64")) {
            return Float.SIZE;
        } else {
            throw new UnsupportedOperationException("HDF5 Type '" + type + "' not supported yet.");
        }
    }

    private static boolean hdf5DataSetExists(
            IHDF5Reader reader,
            StringBuilder hdf5DataSet) {
        String dataSets = "";
        boolean dataSetExists;
        if (reader.object().isDataSet(hdf5DataSet.toString())) {
            return true;
        } else {
            List<String> hdf5Header = reader.getGroupMembers("/");
            hdf5Header.replaceAll(String::toUpperCase);
            dataSetExists = Arrays.stream( FileInfos.HDF5_DATASET_NAMES ).parallel().anyMatch( x -> hdf5Header.contains(x.toUpperCase()));
            List<String> head = Arrays.stream( FileInfos.HDF5_DATASET_NAMES ).parallel().filter( x -> hdf5Header.contains(x.toUpperCase())).collect(Collectors.toList());
            if ( head.size() == 0 )
                head = hdf5Header;
            hdf5DataSet.delete(0, hdf5DataSet.length());
            hdf5DataSet.append(head.get(0));
        }

        return dataSetExists;
    }

    private static boolean setHDF5DatasetViaUI( IHDF5Reader reader,
                                                StringBuilder hdf5DataSet) {

        List<String> hdf5Header = reader.getGroupMembers("/");

        final GenericDialog gd = new GenericDialog( "Choose HDF5 Dataset" );

        gd.addChoice( "HDF5 Dataset",
                hdf5Header.toArray(new String[0]),
                hdf5Header.get( 0 )  );

        gd.showDialog();
        if( gd.wasCanceled() ) return false;

        hdf5DataSet.delete(0, hdf5DataSet.length());
        hdf5DataSet.append( gd.getNextChoice() );
        return true;
    }

    public static String dsInfoToTypeString (HDF5DataSetInformation dsInfo ) {  //TODO : DUPLICATE CODE! Fix it! --ashis
        HDF5DataTypeInformation dsType = dsInfo.getTypeInformation();
        String typeText = "";

        if (dsType.isSigned() == false) {
            typeText += "u";
        }

        switch (dsType.getDataClass()) {
            case INTEGER:
                typeText += "int" + 8 * dsType.getElementSize();
                break;
            case FLOAT:
                typeText += "float" + 8 * dsType.getElementSize();
                break;
            default:
                typeText += dsInfo.toString();
        }
        return typeText;
    }

    public static boolean setVoxelSizeFromLuxendoHDF5( IHDF5Reader reader, FileInfos fileInfos )
    {
        if ( reader.hasAttribute( "/" + fileInfos.h5DataSetName, "element_size_um" ) )
        {
            final double[] voxelSizeZYX = reader.float64().getArrayAttr( "/" + fileInfos.h5DataSetName, "element_size_um");
            double[] voxelSizeXYZ = new double[ 3 ];

            // reorder the dimensions
            for ( int d = 0; d < 3; d++ )
                voxelSizeXYZ[ d ] = voxelSizeZYX[ 2 - d];

            fileInfos.voxelUnit = "micrometer";
            fileInfos.voxelSize = voxelSizeXYZ;

            return true;
        }
        else
        {
            return false;
        }
    }

    public static String hdf5InfoToString(HDF5DataSetInformation dsInfo)
    {
        //
        // Code copied from Ronneberger
        //
        HDF5DataTypeInformation dsType = dsInfo.getTypeInformation();
        String typeText = "";

        if (dsType.isSigned() == false) {
            typeText += "u";
        }

        switch( dsType.getDataClass())
        {
            case INTEGER:
                typeText += "int" + 8*dsType.getElementSize();
                break;
            case FLOAT:
                typeText += "float" + 8*dsType.getElementSize();
                break;
            default:
                typeText += dsInfo.toString();
        }
        return typeText;
    }

    public static boolean checkDataCubeSize( int nz, long nx, int ny )
    {
        long maxSize = (1L << 31) - 1;
        long nPixels = nx * ny * nz;
        if (nPixels > maxSize) {
            Logger.error("H5 Loader: nPixels > 2^31 => Currently not supported.");
            return false;
        }

        return true;
    }
}

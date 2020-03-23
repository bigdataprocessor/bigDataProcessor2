package de.embl.cba.bdp2.save;

import net.imglib2.RandomAccessibleInterval;

/**
 * Created by tischi on 22/05/17.
 */
public class SavingSettings {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final String TIFF_PLANES = "Tiff Planes";
    public static final String TIFF_VOLUMES = "Tiff Volumes";
    public static final String HDF_5_VOLUMES = "Hdf5 Volumes";
    public static final String IMARIS_VOLUMES = "Imaris Volumes";

    // TODO: remove the image itself from the settings
    public RandomAccessibleInterval rai;
    public double[] voxelSpacing;
    public String voxelUnit;

    // TODO: also remove the binning
    public String bin;

    public static final String COMPRESSION_LZW = "LZW";
    public static final String COMPRESSION_ZLIB = "ZLIB";
    public static final String COMPRESSION_NONE = "None";
    public boolean saveVolumes;
    public String volumesFilePathStump;

    public boolean saveProjections;
    public String projectionsFilePathStump;

    public boolean convertTo8Bit;
    public int mapTo0, mapTo255;
    public boolean convertTo16Bit;
    public boolean gate;
    public int gateMin, gateMax;
    public String parentDirectory;
    public FileType fileType;
    public String compression;
    public int compressionLevel;
    public int rowsPerStrip;
    public int numIOThreads = 1;
    public int numProcessingThreads = 1;

	public enum FileType
    {
        TIFF_PLANES( SavingSettings.TIFF_PLANES ),
        TIFF_VOLUMES( SavingSettings.TIFF_VOLUMES ),
        HDF5_VOLUMES( HDF_5_VOLUMES ),
        IMARIS_VOLUMES( SavingSettings.IMARIS_VOLUMES );

        private final String text;

        FileType( String s )
        {
            text = s;
        }

        @Override
        public String toString()
        {
            return text;
        }

        public static FileType getEnum( String value )
        {
            for ( FileType v : values() )
                if ( v.toString().equalsIgnoreCase( value ) ) return v;
            throw new IllegalArgumentException();
        }
    }

    /**
     * Loads minimum settings for save TIFF as Planes.
     * Useful for testing purposes.
     * @return SavingSettings
     */
    public static SavingSettings getDefaults() {
        SavingSettings savingSettings = new SavingSettings();
        savingSettings.bin = "1,1,1";
        savingSettings.voxelSpacing = new double[]{1,1,1};
        savingSettings.voxelUnit = "pixel";
        savingSettings.saveVolumes = true;
        savingSettings.fileType = FileType.TIFF_PLANES;
        savingSettings.volumesFilePathStump = "/Users/tischer/Desktop/bdp2-out/image";
        savingSettings.compression = COMPRESSION_NONE;
        savingSettings.numProcessingThreads = (int) Math.ceil( Math.sqrt( AVAILABLE_PROCESSORS ) + 1 );
        savingSettings.numIOThreads = savingSettings.numProcessingThreads;

        return savingSettings;
    }
}

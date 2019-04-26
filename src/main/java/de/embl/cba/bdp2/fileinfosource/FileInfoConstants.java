package de.embl.cba.bdp2.fileinfosource;

import de.embl.cba.imaris.ImarisUtils;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

public class FileInfoConstants {

    public static final String LOAD_CHANNELS_FROM_FOLDERS = "Channels from Subfolders";
    public static final String EM_TIFF_SLICES = "EM Tiff Slices";
    public static final String LEICA_SINGLE_TIFF = "Leica Single Tiff";
    public static final String SINGLE_CHANNEL_TIMELAPSE = "Single Channel Movie";

    public static final String PATTERN_1= "<Z0000-0009>.tif"; // make pattern class
    public static final String PATTERN_2= ".*--C<c>--T<t>.tif";
    public static final String PATTERN_3= ".*--C<c>--T<t>.h5";
    public static final String PATTERN_4= ".*_C<c>_T<t>.tif";
    public static final String PATTERN_5= ".*--t<t>--Z<z>--C<c>.tif";
    public static final String PATTERN_6= "Cam_<c>_<t>.h5";
    public static final int PROGRESS_UPDATE_MILLISECONDS = 200;
    public static final int TOTAL_AXES = 5;
    public static final AxisType[] AXES_ORDER = {Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME};
    public static final int MAX_ALLOWED_IMAGE_DIMS = AXES_ORDER.length;
    public static final String CROPPED_VIEW_NAME = "cropped";
    public static final String UNSIGNED_BYTE_VIEW_NAME = "8-bit converted";
    public static final String TRACKED_IMAGE_NAME = "tracked";
    public static final String IMAGE_NAME = "image";
    public static final String BB_CROP_BUTTON_LABEL= "Crop";
    public static final String BB_TRACK_BUTTON_LABEL= "Select";
    public static final String[] BOUNDING_BOX_AXES_3D ={"x","y","z"};
    public static final String[] BOUNDING_BOX_AXES_4D ={"x","y","z","t"};
    public static final long HDF5_BLOCK_SIZE_2D = 128;
    public static final long HDF5_BLOCK_SIZE_3D = 32;

    public static final String[] POSSIBLE_HDF5_DATASETNAMES = new String[] {"None",
            "Data","Data111",
            ImarisUtils.RESOLUTION_LEVEL +"0/Data",
            ImarisUtils.RESOLUTION_LEVEL +"1/Data",
            ImarisUtils.RESOLUTION_LEVEL +"2/Data",
            ImarisUtils.RESOLUTION_LEVEL +"3/Data",
            "ITKImage/0/VoxelData", "Data222", "Data444"};

}

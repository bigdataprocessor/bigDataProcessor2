package de.embl.cba.bdp2.process.calibrate;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = AbstractImageProcessingCommand.class, name = SetVoxelSizeCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PROCESS_PATH + SetVoxelSizeCommand.COMMAND_FULL_NAME )
public class SetVoxelSizeCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Set Voxel Size...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Unit", choices = { "micrometer", "nanometer" }, persist = false)
    public String unit = "micrometer";

    @Parameter(label = "Voxel size X", persist = false)
    public double voxelSizeX = 1.0;
    public static String VOXEL_SIZE_X_PARAMETER = "voxelSizeX";

    @Parameter(label = "Voxel size Y", persist = false)
    public double voxelSizeY = 1.0;
    public static String VOXEL_SIZE_Y_PARAMETER = "voxelSizeY";

    @Parameter(label = "Voxel size Z", persist = false)
    public double voxelSizeZ = 1.0;
    public static String VOXEL_SIZE_Z_PARAMETER = "voxelSizeZ";

    public void run()
    {
        outputImage = BigDataProcessor2.setVoxelSize( inputImage, new double[]{ voxelSizeX, voxelSizeY, voxelSizeZ }, BioFormatsMetaDataHelper.getUnitFromString( unit ) );
        log();

        handleOutputImage( false, false );
    }

    private void log()
    {
        Logger.log( COMMAND_FULL_NAME );
        double[] voxelSize = outputImage.getVoxelSize();
        for ( int d = 0; d < 3; d++ )
        {
            Logger.log( "Voxel size [" + d + "]: " + voxelSize[ d ] );
        }
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new CalibrationDialog< R >( imageViewer ).showDialog();
    }
}

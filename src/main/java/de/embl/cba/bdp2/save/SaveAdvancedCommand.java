package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.service.ImageService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;


/**
 * TODO: How to add a HELP button for the regular expression without screwing up the macro recording?
 *
 *
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Save>" + SaveAdvancedCommand.COMMAND_NAME )
public class SaveAdvancedCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_NAME = "BDP2_SaveAdvanced...";

    @Parameter(label = "Input image name", persist = true)
    protected Image< R > inputImage = ImageService.nameToImage.values().iterator().next();
    public static final String INPUT_IMAGE_PARAMETER = "inputImage";

    @Parameter(label = "Saving directory", style = "directory")
    File directory;

    @Parameter(label = "Save volumes")
    boolean saveVolumes;

    @Parameter(label = "Save projections")
    boolean saveProjections;

    @Parameter(label = "File type", choices =
            {
                    SavingSettings.TIFF_VOLUMES,
                    SavingSettings.IMARIS_VOLUMES,
                    SavingSettings.TIFF_PLANES
            })
    String fileType;

    @Parameter(label = "Tiff compression", choices =
            {
                    SavingSettings.COMPRESSION_NONE,
                    SavingSettings.COMPRESSION_ZLIB,
                    SavingSettings.COMPRESSION_LZW
            })
    String tiffCompression;

    @Parameter(label = "Number of I/O threads")
    int numIOThreads;

    @Parameter(label = "Number of processing threads")
    int numProcessingThreads;


    public void run()
    {
        new Thread( () ->
        {
            SavingSettings savingSettings = getSavingSettings();

            BigDataProcessor2.saveImage(
                    inputImage,
                    savingSettings,
                    new LoggingProgressListener( "Frames saved" ) );

        } ).start();
    }

    private SavingSettings getSavingSettings()
    {
        SavingSettings savingSettings = new SavingSettings();

        savingSettings.fileType = SavingSettings.FileType.valueOf( fileType );

        savingSettings.compression = tiffCompression;
        savingSettings.rowsPerStrip = 10;

        savingSettings.saveVolumes = saveVolumes;
        savingSettings.saveProjections = saveProjections;

        savingSettings.volumesFilePathStump = directory + File.separator + "volumes" + File.separator + "volume";
        savingSettings.projectionsFilePathStump = directory + File.separator + "projections" + File.separator + "projection";

        savingSettings.numIOThreads = numIOThreads;
        savingSettings.numProcessingThreads = numProcessingThreads;

        savingSettings.voxelSpacing = inputImage.getVoxelSpacing();
        savingSettings.voxelUnit = inputImage.getVoxelUnit();

        return savingSettings;
    }
}

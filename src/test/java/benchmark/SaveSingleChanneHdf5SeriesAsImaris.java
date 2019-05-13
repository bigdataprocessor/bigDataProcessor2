package benchmark;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.Binner;
import de.embl.cba.bdp2.saving.CachedCellImgReplacer;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;

import java.io.File;

public class SaveSingleChanneHdf5SeriesAsImaris
{

    public static void main(String[] args)
    {
        final BigDataProcessor2 bdp = new BigDataProcessor2();

        final String directory = "/Users/tischer/Documents/isabell-schneider-splitchipmerge/stack_0_channel_0";

        final int numIOThreads = 4; // TODO

        final String loadingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.h5";
        final String dataset = "Data";


        final Image image = bdp.openHdf5Image(
                directory,
                loadingScheme,
                filterPattern,
                dataset );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSpacing( 0.13, 0.13, 1.04 );

        bdp.showImage( image );

        final Image binnedImage = Binner.bin( image, new long[]{ 1, 1, 1, 0, 0 } );
        //   bdp.showImage( bin );

        FileInfos fileInfos =
                new FileInfos(
                        directory,
                        loadingScheme,
                        filterPattern,
                        dataset );

        final CachedCellImg volumeCachedCellImg
                = CachedCellImgReader.getVolumeCachedCellImg( fileInfos );

        final RandomAccessibleInterval replaced =
                new CachedCellImgReplacer( binnedImage.getRai(),
                        volumeCachedCellImg ).get();

        final Image volumeLoaderBinnedImage = new Image<>(
                replaced,
                fileInfos.directory,
                fileInfos.voxelSpacing,
                fileInfos.voxelUnit );

        final File out = new File( "/Users/tischer/Desktop/stack_0_channel_0-asImaris-bdp2/im");

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SavingSettings.FileType.IMARIS_STACKS;
        savingSettings.numIOThreads = 1;
        savingSettings.saveProjections = false;
        savingSettings.saveVolumes = true;
        savingSettings.volumesFilePath = out.toString();

        Utils.saveImageAndWaitUntilDone( bdp, savingSettings, binnedImage );

        Utils.saveImageAndWaitUntilDone( bdp, savingSettings, volumeLoaderBinnedImage );

    }

}

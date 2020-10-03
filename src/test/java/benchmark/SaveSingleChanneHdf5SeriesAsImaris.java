package benchmark;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.CachedCellImgCreator;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.process.bin.Binner;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.save.CachedCellImgReplacer;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.utils.DimensionOrder;
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

        final String loadingScheme = NamingSchemes.SINGLE_CHANNEL_TIMELAPSE;
        final String filterPattern = ".*.h5";
        final String dataset = "Data";

        final Image image = bdp.openImageFromHdf5(
                directory,
                loadingScheme,
                filterPattern,
                dataset);

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSize( 0.13, 0.13, 1.04 );

        bdp.showImage( image);

        final Image binnedImage = Binner.bin( image, new long[]{ 3, 3, 3, 1, 1 } );
        //   bdp.showImage( bin );

        FileInfos fileInfos =
                new FileInfos(
                        directory,
                        loadingScheme,
                        filterPattern,
                        dataset );

        final CachedCellImg volumeCachedCellImg
                = CachedCellImgCreator.createVolumeCachedCellImg( fileInfos, image.getDimensionsXYZCT()[ DimensionOrder.C ] * numIOThreads );

        final RandomAccessibleInterval replaced =
                new CachedCellImgReplacer( binnedImage.getRai(),
                        volumeCachedCellImg ).get();

        final Image volumeLoaderBinnedImage = new Image<>(
                replaced,
                fileInfos.directory,
				fileInfos.channelNames, fileInfos.voxelSize,
                fileInfos.voxelUnit, fileInfos );

        final File out = new File( "/Users/tischer/Desktop/stack_0_channel_0-asImaris-bdp2/im");

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.saveFileType = SavingSettings.SaveFileType.IMARIS_VOLUMES;
        savingSettings.numIOThreads = 1;
        savingSettings.saveProjections = false;
        savingSettings.saveVolumes = true;
        savingSettings.volumesFilePathStump = out.toString();

        BigDataProcessor2.saveImageAndWaitUntilDone( binnedImage, savingSettings);

        BigDataProcessor2.saveImageAndWaitUntilDone( volumeLoaderBinnedImage, savingSettings );

    }

}

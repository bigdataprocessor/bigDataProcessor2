import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerging;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class OpenTwoChannelsFromSplitChipHdf5Series
{
    public static < R extends RealType< R > & NativeType< R > > void main( String[] args)
    {
        // BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();
        final ImageViewer viewer = ViewerUtils
                .getImageViewer( ViewerUtils.BIG_DATA_VIEWER );

        String imageDirectory = "/Users/tischer/Desktop/stack_0_channel_0";

        final FileInfos fileInfos = new FileInfos( imageDirectory,
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5", "Data" );

        fileInfos.voxelSpacing = new double[]{ 1.0, 1.0, 10.0};

        final Image< R > image = CachedCellImgReader.asImage( fileInfos );

        final ArrayList< double[] > centres = new ArrayList<>();
        centres.add( new double[]{ 522, 1143 } );
        centres.add( new double[]{ 1407, 546 } );
        final double[] spans = { 950, 950 };

        final RandomAccessibleInterval< R > colorRAI
                = SplitViewMerging.merge( image.getRai(), centres, spans, fileInfos.voxelSpacing );

        viewer.show(
                colorRAI,
                image.getName(),
                image.getVoxelSpacing(),
                image.getVoxelUnit(),
                true );

    }

}

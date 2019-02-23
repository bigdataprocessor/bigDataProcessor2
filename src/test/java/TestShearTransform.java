import de.embl.cba.bdp2.CachedCellImageCreator;
import de.embl.cba.bdp2.ui.BigDataProcessor;
import de.embl.cba.bdp2.ui.ObliqueMenuDialog;
import de.embl.cba.bdp2.ui.ShearingSettings;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.fileinfosource.FileInfoSource;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestShearTransform {

    public static void main(String[] args)
    {
        String imageDirectory = "src/test/resources/shear_transform_test";
        final FileInfoSource fileInfoSource = new FileInfoSource( imageDirectory, FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
                ".*", "");
        CachedCellImg cachedCellImg = CachedCellImageCreator.create(fileInfoSource, null);

        ImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
                cachedCellImg,
                "input",
                new double[]{1.0, 1.0, 1.0},
                "pixel");
        imageViewer.show();
        imageViewer.setDisplayRange( 0, 800, 0 );


        /**
         * Get sheared image and show it in same viewer
         * (replacing the input image);
         */
        RandomAccessibleInterval sheared = getShearedImage( cachedCellImg, imageViewer );
        imageViewer.show( sheared, "sheared", imageViewer.getVoxelSize(), imageViewer.getCalibrationUnit(), true );
        //imageViewer.setDisplayRange( 0, 800, 0 );


        /**
         * Compute shearing using a 5D Affine Transform.
         * This is much simpler, but also much  slower to compute
         */
    //        RandomAccessibleInterval sheared5D = getShearedImage5D( cachedCellImg, imageViewer );
    //        ImageViewer imageViewer2 = new BdvImageViewer<UnsignedShortType>(
    //                sheared5D,
    //                "sheared5D",
    //                new double[]{1.0, 1.0, 1.0});
    //        imageViewer2.show();
    //        imageViewer2.setDisplayRange( 0, 800, 0 );


    }

    private static RandomAccessibleInterval getShearedImage( CachedCellImg cachedCellImg, ImageViewer imageViewer )
    {
        ShearingSettings shearingSettings = new ShearingSettings();
        ObliqueMenuDialog dialog = new ObliqueMenuDialog(imageViewer);
        dialog.getShearingSettings( shearingSettings ); // sets default values.
        return BigDataProcessor.shearImage( cachedCellImg, shearingSettings );
    }

    private static RandomAccessibleInterval getShearedImage5D( CachedCellImg cachedCellImg, ImageViewer imageViewer )
    {
        ShearingSettings shearingSettings = new ShearingSettings();
        ObliqueMenuDialog dialog = new ObliqueMenuDialog(imageViewer);
        dialog.getShearingSettings( shearingSettings ); // sets default values.
        return BigDataProcessor.shearImage5D( cachedCellImg, shearingSettings );
    }
}

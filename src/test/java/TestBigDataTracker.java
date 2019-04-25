import de.embl.cba.bdp2.CachedCellImageCreator;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.fileinfosource.FileInfoSource;
import de.embl.cba.bdp2.motioncorrection.BigDataTracker;
import de.embl.cba.bdp2.motioncorrection.TrackingSettings;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import javafx.geometry.Point3D;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class TestBigDataTracker {

    public static void main(String[] args) {

        String imageDirectory = "src/test/resources/tiff-nc2-nt3-tracking/";
        final FileInfoSource fileInfoSource = new FileInfoSource(imageDirectory, FileInfoConstants.LOAD_CHANNELS_FROM_FOLDERS,
                ".*", "");
        CachedCellImg cachedCellImg = CachedCellImageCreator.create(fileInfoSource, null);
        ImageViewer imageViewer = new BdvImageViewer<UnsignedShortType>(
                cachedCellImg,
                "input",
                new double[]{1.0, 1.0, 1.0},
                "pixel");
        imageViewer.show();

        BigDataTracker bdt = new BigDataTracker();
        TrackingSettings trackingSettings = getTrackingSettingWithDummyValues(imageViewer);
        //Test for CROSS_CORRELATION tracking
        trackingSettings.trackingMethod = TrackingSettings.CROSS_CORRELATION;
        bdt.trackObject(trackingSettings, imageViewer);
        //Test for CENTER of MASS tracking
        trackingSettings.trackingMethod = TrackingSettings.CENTER_OF_MASS;
        bdt.trackObject(trackingSettings, imageViewer);
    }

    private static TrackingSettings getTrackingSettingWithDummyValues(ImageViewer imageViewer) {
        Point3D maxDisplacement = new Point3D(20, 20, 1);
        TrackingSettings trackingSettings = new TrackingSettings();
        trackingSettings.imageRAI = imageViewer.getRai();
        trackingSettings.maxDisplacement = maxDisplacement;
        trackingSettings.objectSize = new Point3D(200, 200, 10);
        trackingSettings.trackingFactor = 1.0 + 2.0 * maxDisplacement.getX() /
                trackingSettings.objectSize.getX();
        trackingSettings.iterationsCenterOfMass = (int) Math.ceil(Math.pow(trackingSettings.trackingFactor, 2));
        trackingSettings.pMin = new Point3D(5, 7, 15);
        trackingSettings.pMax = new Point3D(30, 30, 36);
        trackingSettings.tStart = 0;
        trackingSettings.intensityGate = new int[]{75, -1};
        trackingSettings.imageFeatureEnhancement = Utils.ImageFilterTypes.NONE.toString();
        trackingSettings.nt = -1;
        trackingSettings.channel=0;
        return trackingSettings;
    }
}

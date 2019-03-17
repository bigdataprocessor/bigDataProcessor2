package de.embl.cba.bdp2.viewers;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandleFrame;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdp2.boundingbox.BoundingBoxDialog;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.ui.DisplaySettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import javax.swing.*;

public class BdvImageViewer<T extends RealType<T> & NativeType<T>> implements ImageViewer {

    private RandomAccessibleInterval<T> rai;
    private double[] voxelSize;
    private String imageName;

    private BdvStackSource<T> bdvSS;
    private String calibrationUnit;
    private BdvGrayValuesOverlay overlay;

    public BdvImageViewer() {
    }

    // TODO: wrap RAI into a "PhysicalImg" with voxelSize and Calibration
    public BdvImageViewer(
            RandomAccessibleInterval<T> rai,
            String imageName,
            double[] voxelSize,
            String calibrationUnit) {
        this.imageName = imageName;
        this.rai = rai;
        this.voxelSize = voxelSize;
        this.calibrationUnit = calibrationUnit;
    }


    @Override
    public FinalInterval get5DIntervalFromUser() {
        BoundingBoxDialog showBB = new BoundingBoxDialog(this.bdvSS.getBdvHandle());
        //showBB.show( rai, voxelSize, BB_TRACK_BUTTON_LABEL,true);
        showBB.show(rai, voxelSize);
        FinalInterval interval;
        if (showBB.selectedMax != null && showBB.selectedMin != null) {
            long[] minMax = {
                    (long) (showBB.selectedMin[BoundingBoxDialog.X] / voxelSize[ DimensionOrder.X]),
                    (long) (showBB.selectedMin[BoundingBoxDialog.Y] / voxelSize[ DimensionOrder.Y]),
                    (long) (showBB.selectedMin[BoundingBoxDialog.Z] / voxelSize[ DimensionOrder.Z]),
                    rai.min( DimensionOrder.C),
                    showBB.selectedMin[BoundingBoxDialog.T],
                    (long) (showBB.selectedMax[BoundingBoxDialog.X] / voxelSize[ DimensionOrder.X]),
                    (long) (showBB.selectedMax[BoundingBoxDialog.Y] / voxelSize[ DimensionOrder.Y]),
                    (long) (showBB.selectedMax[BoundingBoxDialog.Z] / voxelSize[ DimensionOrder.Z]),
                    rai.max( DimensionOrder.C),
                    showBB.selectedMax[BoundingBoxDialog.T]};
            interval= Intervals.createMinMax(minMax);
        }else{
            interval =  null;
        }
        return interval;
    }

    @Override
    public ImageViewer newImageViewer() {
        return new BdvImageViewer<T>();
    }

    @Override
    public RandomAccessibleInterval<T> getRai() {
        return rai;
    }

    @Override
    public double[] getVoxelSize() {
        return voxelSize;
    }

    @Override
    public String getImageName() {
        return imageName;
    }


    @Override // TODO: remove this...
    public void repaint(AffineTransform3D viewerTransform) {
        this.bdvSS.getBdvHandle().getViewerPanel().setCurrentViewerTransform(viewerTransform);
    }

    @Override
    public void repaint() {
        this.bdvSS.getBdvHandle().getViewerPanel().requestRepaint();
    }

    @Override
    public void show() {
        showImageInViewer(rai, imageName, voxelSize, calibrationUnit);
    }

    @Override
    public void show(
            RandomAccessibleInterval rai,
            String imageName,
            double[] voxelSize,
            String calibrationUnit,
            boolean autoContrast)
    {
        if (this.bdvSS != null)
            removeAllSourcesFromBdv();

        showImageInViewer(rai, imageName, voxelSize, calibrationUnit);

        if (autoContrast)
            doAutoContrastPerChannel();
    }


    private void removeAllSourcesFromBdv() {
        int nSources = this.bdvSS.getBdvHandle().getViewerPanel().getState().getSources().size();
        for (int source = 0; source < nSources; ++source) {
            SourceAndConverter scnv = this.bdvSS.getBdvHandle().getViewerPanel().getState().getSources().get(0);
            this.bdvSS.getBdvHandle().getViewerPanel().removeSource(scnv.getSpimSource());
            //source is always 0 (zero) because SourceAndConverter object gets removed from bdvSS.
            //Hence source is always at position 0 of the bdvSS.
        }

        int nChannels = this.getBdvSS().getBdvHandle().getSetupAssignments().getConverterSetups().size();
        for (int channel = 0; channel < nChannels; ++channel) {
            ConverterSetup converterSetup = this.getBdvSS().getBdvHandle().getSetupAssignments().getConverterSetups().get(0);
            this.bdvSS.getBdvHandle().getSetupAssignments().removeSetup(converterSetup);
            //channel is always 0 (zero) because converterSetup object gets removed from bdvSS.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    public void addMenus(BdvMenus menus) {
        menus.setImageViewer(this);
        for (JMenu menu : menus.getMenus()) {
            ((BdvHandleFrame) this.bdvSS.getBdvHandle()).getBigDataViewer().getViewerFrame().getJMenuBar().add((menu));
        }
        ((BdvHandleFrame) this.bdvSS.getBdvHandle()).getBigDataViewer().getViewerFrame().getJMenuBar().updateUI();
    }

    @Override
    public void setDisplayRange(double min, double max, int channel) {
        final ConverterSetup converterSetup = this.bdvSS.getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
        this.bdvSS.getBdvHandle().getSetupAssignments().removeSetup(converterSetup);
        converterSetup.setDisplayRange(min, max);
        this.bdvSS.getBdvHandle().getSetupAssignments().addSetup(converterSetup);

    }

    /**
     * Returns min and max pixel values of the center slice of the first time point for the RandomAccessibleInterval
     * as a DisplaySettings object of the requested channel.
     */
    @Override
    public DisplaySettings getDisplaySettings(int channel) {
        double min, max;
        if (this.rai != null) {
            RandomAccessibleInterval raiStack = Views.hyperSlice(
                    Views.hyperSlice(this.rai, DimensionOrder.T, 0),
                    DimensionOrder.C,
                    channel);
            final long stackCenter = (raiStack.max( DimensionOrder.Z) - raiStack.min( DimensionOrder.Z)) / 2 + raiStack.min( DimensionOrder.Z);
            IntervalView<T> ts = Views.hyperSlice(
                    raiStack,
                    DimensionOrder.Z,
                    stackCenter);
            Cursor<T> cursor = Views.iterable(ts).cursor();
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;
            double value;
            while (cursor.hasNext()) {
                value = cursor.next().getRealDouble();
                if (value < min) min = value;
                if (value > max) max = value;
            }
        } else {
            max = 0;
            min = 0;
        }
        return new DisplaySettings(min, max);
    }

    @Override
    public void doAutoContrastPerChannel() {
        int nChannels = (int) this.getRai().dimension( DimensionOrder.C);
        for (int channel = 0; channel < nChannels; ++channel) {
            DisplaySettings setting = getDisplaySettings(channel);
            setDisplayRange(setting.getMinValue(), setting.getMaxValue(), 0);
            //channel is always 0 (zero) because converterSetup object gets removed and added at the end of bdvSS in setDisplayRange method.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    @Override
    public String getCalibrationUnit() {
        return calibrationUnit;
    }

    @Override
    public AffineTransform3D getViewerTransform()
    {
        if ( bdvSS != null )
        {
            final AffineTransform3D transform3D = new AffineTransform3D();
            bdvSS.getBdvHandle().getViewerPanel()
                    .getState().getViewerTransform( transform3D );
            return transform3D.copy();
        }
        else
           return null;
    }

    @Override
    public void setViewerTransform( AffineTransform3D viewerTransform )
    {
        bdvSS.getBdvHandle().getViewerPanel().setCurrentViewerTransform( viewerTransform );
        bdvSS.getBdvHandle().getViewerPanel().requestRepaint();
    }

    public void replicateViewerContrast(ImageViewer newImageView) {
        int nChannels = (int) this.getRai().dimension( DimensionOrder.C);
        for (int channel = 0; channel < nChannels; ++channel) {
            ConverterSetup converterSetup = this.getBdvSS().getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
            newImageView.setDisplayRange(converterSetup.getDisplayRangeMin(), converterSetup.getDisplayRangeMax(), 0);
            //channel is always 0 (zero) because converterSetup object gets removed and added at the end of bdvSS in setDisplayRange method.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    public int getCurrentTimePoint() {
        return this.bdvSS.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();
    }

    @Override
    public void shiftImageToCenter(double[] centerCoordinates) {
        AffineTransform3D sourceTransform = new AffineTransform3D();
        int width = this.bdvSS.getBdvHandle().getViewerPanel().getWidth();
        int height = this.bdvSS.getBdvHandle().getViewerPanel().getHeight();
        centerCoordinates[0] = (width / 2.0 + centerCoordinates[0]);
        centerCoordinates[1] = (height / 2.0 - centerCoordinates[1]);
        centerCoordinates[2] = -centerCoordinates[2];
        sourceTransform.translate(centerCoordinates);
        repaint(sourceTransform);
    }

    public BdvStackSource getBdvSS() {
        return bdvSS;
    }

    private void showImageInViewer(
            RandomAccessibleInterval rai,
            String imageName,
            double[] voxelSize,
            String calibrationUnit)
    {

        AffineTransform3D transform3D = getViewerTransform();
        final AffineTransform3D scaling = getScalingTransform( voxelSize );

        bdvSS = BdvFunctions.show(
                asVolatile(rai),
                imageName,
                BdvOptions.options().axisOrder(AxisOrder.XYZCT)
                        .addTo(bdvSS).sourceTransform(scaling));

        if ( transform3D != null ) setViewerTransform( transform3D );
        setColors();
        addGrayValueOverlay();

        this.imageName = imageName;
        this.calibrationUnit = calibrationUnit;
        this.rai = rai;
        this.voxelSize = voxelSize;
    }

    private AffineTransform3D getScalingTransform( double[] voxelSize )
    {
        final AffineTransform3D scaling = new AffineTransform3D();

        for (int d = 0; d < 3; d++)
            scaling.set(voxelSize[d], d, d);
        return scaling;
    }

    private void setColors()
    {
        final int numSources = bdvSS.getSources().size();
        if ( numSources > 1 )
        {
            for ( int sourceIndex = 0; sourceIndex < numSources; sourceIndex++ )
            {
                final ConverterSetup converterSetup =
                        bdvSS.getBdvHandle().getSetupAssignments().getConverterSetups().get( sourceIndex );

                converterSetup.setColor( getColor( sourceIndex, numSources ) );
            }

        }
    }

    private ARGBType getColor( int sourceIndex, int numSources )
    {

        switch ( sourceIndex )
        {
            case 0:
                return new ARGBType( ARGBType.rgba( 0, 255, 0, 255 / numSources ) );
            case 1:
                return new ARGBType( ARGBType.rgba( 255, 0, 255, 255 / numSources ) );
            case 2:
                return new ARGBType( ARGBType.rgba( 0, 255, 255, 255 / numSources ) );
            case 3:
                return new ARGBType( ARGBType.rgba( 255, 0, 0, 255 / numSources ) );
            default:
                return new ARGBType( ARGBType.rgba( 255, 255, 255, 255 / numSources ) );
        }

    }

    private RandomAccessibleInterval asVolatile(RandomAccessibleInterval rai) {
        try {
            rai = VolatileViews.wrapAsVolatile(rai);
        } catch (IllegalArgumentException e) { //Never mind!
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rai;
    }

    private void addGrayValueOverlay() {
        if (overlay == null) {
            overlay = new BdvGrayValuesOverlay(this.bdvSS, 20, "Courier New");
        }
        BdvFunctions.showOverlay(overlay,
                "GrayOverlay",
                BdvOptions.options().addTo(bdvSS));

    }
}
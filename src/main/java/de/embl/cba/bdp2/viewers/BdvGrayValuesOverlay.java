package de.embl.cba.bdp2.viewers;

import bdv.util.Bdv;
import bdv.util.BdvOverlay;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.RealPoint;
import net.imglib2.type.numeric.ARGBType;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Map;

public class BdvGrayValuesOverlay extends BdvOverlay implements MouseMotionListener {
    private final Bdv bdv;
    private final String fontName;
    private final int fontSize;
    private ArrayList<Double> values;
    private ArrayList<ARGBType> colors;

    public BdvGrayValuesOverlay(Bdv bdv, int fontSize,String fontFace) {
        super();
        this.bdv = bdv;
        this.fontName = (fontFace != null && !fontFace.isEmpty() && !fontFace.trim().isEmpty()) ? fontFace : "Default";
        bdv.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener(this);
        this.fontSize = fontSize;
        values = new ArrayList<>();
        colors = new ArrayList<>();
    }

    private void setValuesAndColors(ArrayList<Double> values, ArrayList<ARGBType> colors) {
        this.values = values;
        this.colors = colors;
    }

    @Override
    protected void draw(final Graphics2D g) {
        int[] stringPosition = new int[]{(int) g.getClipBounds().getWidth() - 107, 20 + fontSize};//Handcrafted

        for (int i = 0; i < values.size(); ++i) {
            final int colorIndex = colors.get(i).get();
            g.setColor(new Color(ARGBType.red(colorIndex), ARGBType.green(colorIndex), ARGBType.blue(colorIndex)));
            g.setFont(new Font( this.fontName, Font.PLAIN, fontSize));
            g.drawString("Value: " + values.get(i), stringPosition[0], stringPosition[1] + fontSize * i + 5);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        final RealPoint realPoint = new RealPoint(3);
        bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates(realPoint);
        final int currentTimepoint = bdv.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();
        final Map<Integer, Double> pixelValuesOfActiveSources =
                BdvUtils.getPixelValuesOfActiveSources(bdv, realPoint, currentTimepoint);

        ArrayList<Double> values = new ArrayList<>();
        ArrayList<ARGBType> colors = new ArrayList<>();

        for (int sourceId : pixelValuesOfActiveSources.keySet()) {
            values.add(pixelValuesOfActiveSources.get(sourceId));
            final ARGBType color = BdvUtils.getColor(bdv, sourceId);
            final int colorIndex = color.get();
            if (colorIndex == 0) {
                colors.add(new ARGBType(ARGBType.rgba(255, 255, 255, 255)));
            }else{
                colors.add(color);
            }
        }
        setValuesAndColors(values, colors);
    }
}

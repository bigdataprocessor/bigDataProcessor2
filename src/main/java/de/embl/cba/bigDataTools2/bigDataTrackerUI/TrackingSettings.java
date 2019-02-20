package de.embl.cba.bigDataTools2.bigDataTrackerUI;

import javafx.geometry.Point3D;
import net.imglib2.RandomAccessibleInterval;

/**
 * Created by tischi on 14/04/17.
 */
public class TrackingSettings {

    public RandomAccessibleInterval imageRAI;
    public String trackingMethod;
    public Point3D objectSize;
    public Point3D maxDisplacement;
    public Point3D pMin;
    public Point3D pMax;
    public Point3D subSamplingXYZ;
    public int subSamplingT;
    public int iterationsCenterOfMass;
    public int tStart;
    public int nt;
    public int channel;
    public double trackingFactor;
    public int[] intensityGate =new int[2];
    public int viewFirstNProcessedRegions;
    public String imageFeatureEnhancement;
    double[] voxelSize;



}

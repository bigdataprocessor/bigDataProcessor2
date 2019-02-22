package de.embl.cba.bdp2.ui;

public class DisplaySettings {

    private double minValue;
    private double maxValue;

    public DisplaySettings(double min, double max) {
        this.minValue = min;
        this.maxValue = max;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }
}

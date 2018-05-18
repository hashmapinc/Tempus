package com.hashmapinc.server.extensions.core.plugin.telemetry.dataquality.data;

public class AggregatedMetaData {
    private double avgFrequency;
    private double maxFrequency;
    private double minFrequency;
    private double meanFrequency;
    private double medianFrequency;

    public AggregatedMetaData(double avgFrequency, double maxFrequency, double minFrequency, double meanFrequency, double medianFrequency) {
        this.avgFrequency = avgFrequency;
        this.maxFrequency = maxFrequency;
        this.minFrequency = minFrequency;
        this.meanFrequency = meanFrequency;
        this.medianFrequency = medianFrequency;
    }

    public double getAvgFrequency() {
        return avgFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public double getMeanFrequency() {
        return meanFrequency;
    }

    public double getMedianFrequency() {
        return medianFrequency;
    }
}

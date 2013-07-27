package common.statistics;

import java.util.ArrayList;

import common.algorithms.gaussian.MultivariateNormalDistribution;


public class OneDimensionalDataStatistics
{
    private final double mean;
    private final double backgroundMean;
    private final double variance;
    
    public OneDimensionalDataStatistics(double[] data)
    {
        this.mean = calculateMean(data);
        this.backgroundMean = calculateBackgroundMean(data, this.mean);
        this.variance = calculateVariance(data, this.mean);
    }
    public OneDimensionalDataStatistics(ArrayList<Double> data)
    {
        this.mean = calculateMean(data);
        this.backgroundMean = calculateBackgroundMean(data, this.mean);
        this.variance = calculateVariance(data, this.mean);
    }
    
    public double getMean() { return this.mean; }
    public double getBackgroundMean() { return this.backgroundMean; }
    public double getVariance() { return this.variance; }
    public double getStandardDeviation() { return Math.sqrt(this.variance); }
    public MultivariateNormalDistribution getDistribution()
    {
        return new MultivariateNormalDistribution(
                new double[]{getMean()}, new double[][]{{getVariance()}});
    }

    private double calculateVariance(double[] data, double mean)
    {
        double ret = 0;
        for (int i = 0; i < data.length; ++i) {
            ret += (data[i] - mean) * (data[i] - mean);
        }
        return ret / data.length;
    }

    private double calculateBackgroundMean(double[] data, double mean)
    {
        double ret = 0;
        int count = 0;
        for (int i = 0; i < data.length; ++i) {
            if (data[i] >= mean) continue;
            ret += data[i];
            ++count;
        }
        return ret / count;
    }

    private double calculateMean(double[] data)
    {
        double ret = 0;
        for (int i = 0; i < data.length; ++i) {
            ret += data[i];
        }
        return ret / data.length;
    }
    private double calculateVariance(ArrayList<Double> data, double mean)
    {
        double ret = 0;
        for (Double value : data) {
            ret += (value - mean) * (value - mean);
        }
        return ret / data.size();
    }
    private double calculateBackgroundMean(ArrayList<Double> data, double mean)
    {
        double ret = 0;
        int count = 0;
        for (Double value : data) {
            if (value >= mean) continue;
            ret += value;
            ++count;
        }
        return ret / count;
    }
    private double calculateMean(ArrayList<Double> data)
    {
        double ret = 0;
        for (Double value : data) {
            ret += value;
        }
        return ret / data.size();
    }
}

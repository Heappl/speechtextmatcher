package algorithms;

import java.util.ArrayList;

public class MultivariateDataStatistics
{
    private final double[] mean;
    private final double[][] covariances;
    
    public MultivariateDataStatistics(double[][] data)
    {
        this.mean = calculateMean(data);
        this.covariances = calculateCovariances(data, this.mean);
    }
    public MultivariateDataStatistics(ArrayList<double[]> data)
    {
        this.mean = calculateMean(data);
        this.covariances = calculateCovariances(data, this.mean);
    }
    
    public double[] getMean() { return this.mean; }
    public double[][] getCovariances() { return this.covariances; }
    public double[] getVariances()
    {
        double[] ret = new double[mean.length];
        for (int i = 0; i < ret.length; ++i)
            ret[i] = this.covariances[i][i];
        return ret;
    }
    public double[] getStandardDeviations()
    {
        double[] ret = getVariances();
        for (int i = 0; i < ret.length; ++i)
            ret[i] = Math.sqrt(ret[i]);
        return ret;
    }
    public MultivariateNormalDistribution getDistribution()
    {
        return new MultivariateNormalDistribution(getMean(), getCovariances()); 
    }

    private double[] calculateMean(double[][] data)
    {
        if (data.length == 0) return new double[0];
        double[] ret = new double[data[0].length];
        for (double[] value : data)
            for (int i = 0; i < ret.length; ++i)
                ret[i] += value[i];
        for (int i = 0; i < ret.length; ++i)
            ret[i] /= data.length;
        return ret;
    }
    private double[] calculateMean(ArrayList<double[]> data)
    {
        if (data.size() == 0) return new double[0];
        double[] ret = new double[data.get(0).length];
        for (double[] value : data)
            for (int i = 0; i < ret.length; ++i)
                ret[i] += value[i];
        for (int i = 0; i < ret.length; ++i)
            ret[i] /= data.size();
        return ret;
    }
    private double[][] calculateCovariances(ArrayList<double[]> data, double[] mean)
    {
        if (data.size() == 0) return new double[0][0];
        int d = data.get(0).length;
        double[][] ret = new double[d][d];
        for (double[] value : data)
            for (int i = 0; i < d; ++i)
                for (int j = 0; j < d; ++j)
                    ret[i][j] += (value[i] - mean[i]) * (value[j] - mean[j]);
        for (int i = 0; i < d; ++i)
            for (int j = 0; j < d; ++j)
                ret[i][j] /= data.size();
        return ret;
    }
    private double[][] calculateCovariances(double[][] data, double[] mean)
    {
        if (data.length == 0) return new double[0][0];
        int d = data[0].length;
        double[][] ret = new double[d][d];
        for (double[] value : data)
            for (int i = 0; i < d; ++i)
                for (int j = 0; j < d; ++j)
                    ret[i][j] += (value[i] - mean[i]) * (value[j] - mean[j]);
        for (int i = 0; i < d; ++i)
            for (int j = 0; j < d; ++j)
                ret[i][j] /= data.length;
        return ret;
    }
}

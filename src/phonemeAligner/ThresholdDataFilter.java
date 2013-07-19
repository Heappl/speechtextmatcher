package phonemeAligner;

import java.util.ArrayList;

import phonemeAligner.powerBased.TimedData;

public class ThresholdDataFilter
{
    private final double[] threshold;
    private final int exceedingRequiredPoints;
    
    public ThresholdDataFilter(double[] threshold, double exceedingPercent)
    {
        this.threshold = threshold;
        this.exceedingRequiredPoints =
                (int)Math.floor(threshold.length * (exceedingPercent / 100.0));
    }
    public ThresholdDataFilter(double[] threshold, int exceedingRequiredPoints)
    {
        this.threshold = threshold;
        this.exceedingRequiredPoints = exceedingRequiredPoints;
    }
    
    public double[][] filter(double[][] data)
    {
        ArrayList<double[]> auxData = new ArrayList<double[]>();
        return filter(auxData).toArray(new double[0][]);
    }
    
    public ArrayList<double[]> filter(ArrayList<double[]> data)
    {
        ArrayList<double[]> ret = new ArrayList<double[]>();
        for (double[] point : data) {
            if (overThreshold(point))
                ret.add(point);
        }
        return ret;
    }
    public ArrayList<TimedData> filterTimed(ArrayList<TimedData> data)
    {
        ArrayList<TimedData> ret = new ArrayList<TimedData>();
        for (TimedData point : data) {
            if (overThreshold(point.getData()))
                ret.add(point);
        }
        return ret;
    }

    private boolean overThreshold(double[] point)
    {
        int count = 0;
        for (int i = 0; i < point.length; ++i)
            if (point[i] > this.threshold[i])
                ++count;
        return (count >= exceedingRequiredPoints);
    }
}

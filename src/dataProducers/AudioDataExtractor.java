package dataProducers;

import java.util.ArrayList;

public class AudioDataExtractor implements IWaveObserver
{
    ArrayList<double[]> data = new ArrayList<double[]>();
    double lastTime = 0;
    final double minTime;
    final double maxTime;
    
    public AudioDataExtractor(double minTime, double maxTime)
    {
        this.maxTime = maxTime;
        this.minTime = minTime;
    }
    public AudioDataExtractor()
    {
        this.maxTime = Double.POSITIVE_INFINITY;
        this.minTime = Double.NEGATIVE_INFINITY;
    }
    
    @Override
    public void process(double startTime, double endTime, double[] values)
    {
        lastTime = endTime;
        if (startTime > maxTime) return;
        if (endTime < minTime) return;
        data.add(values);
    }
    
    public ArrayList<double[]> getAllData()
    {
        return data;
    }
    
    public double getTotalTime()
    {
        return this.lastTime;
    }
}

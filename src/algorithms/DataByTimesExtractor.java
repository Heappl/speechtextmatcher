package algorithms;

import java.util.ArrayList;

import common.DataContainer;

public class DataByTimesExtractor<T>
{
    private final DataContainer<T> data;
    private final double startTime;
    private final double stepTime;
    
    public DataByTimesExtractor(DataContainer<T> data, double totalTime, double startTime)
    {
        this.data = data;
        this.startTime = startTime;
        this.stepTime = totalTime / data.size();
    }
    
    public T extract(double time)
    {
        return data.get(findIndex(time, 0, data.size() - 1));
    }
    
    public ArrayList<T> extract(double start, double end)
    {
        int startIndex = findIndex(start, 0, data.size() - 1);
        int endIndex = findIndex(end, 0, data.size() - 1);
        
        ArrayList<T> ret = new ArrayList<T>();
        for (int i = startIndex; i < endIndex; ++i) {
            ret.add(data.get(i));
        }
        return ret;
    }
    
    private int findIndex(double time, int bottom, int top)
    {
        if (bottom == top) return bottom;
        int between = (top + bottom) / 2;
        double auxTime = (getStartTime(between) + getStartTime(between + 1)) / 2;
        if (time < auxTime) return findIndex(time, bottom, between);
        else return findIndex(time, between + 1, top);
    }

    private double getStartTime(int i)
    {
        return i * stepTime + startTime;
    }
}

package phonemeAligner.powerBased;

public class TimedData implements Comparable<TimedData>
{
    double time;
    double[] data;
    
    public TimedData(double time, double[] data)
    {
        this.time = time;
        this.data = data;
    }

    @Override
    public int compareTo(TimedData other)
    {
        if (this.time < other.time) return -1;
        if (this.time > other.time) return 1;
        return 0;
    }
    
    public double[] getData() { return data; }
    public double getTime() { return time; }
}

package common;

public class DoubleDataContainer implements DataContainer<Double>
{
    private double[] data;
    
    public DoubleDataContainer(double[] data)
    {
        this.data = data;
    }

    @Override
    public int size()
    {
        return data.length;
    }

    @Override
    public Double get(int index)
    {
        return data[index];
    }

}

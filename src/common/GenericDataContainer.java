package common;

public class GenericDataContainer<T> implements DataContainer<T>
{
    private final T[] data;
    public GenericDataContainer(T[] data)
    {
        this.data = data;
    }

    @Override
    public int size()
    {
        return data.length;
    }

    @Override
    public T get(int index)
    {
        return data[index];
    }
}

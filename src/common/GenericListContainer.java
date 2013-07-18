package common;

import java.util.List;

public class GenericListContainer<T> implements DataContainer<T>
{
    private List<T> data;
    
    public GenericListContainer(List<T> data)
    {
        this.data = data;
    }

    @Override
    public int size()
    {
        return data.size();
    }

    @Override
    public T get(int index)
    {
        return data.get(index);
    }
}

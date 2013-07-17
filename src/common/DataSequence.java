package common;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;



public class DataSequence implements Iterable<Data>, DataContainer<Data>
{
	private ArrayList<Data> data = new ArrayList<Data>();
	
	public DataSequence() {}
	public DataSequence(ArrayList<Data> data)
	{
		this.data = data;
	}
	
	public DataSequence(Collection<? extends Data> list)
	{
		this.data = new ArrayList<Data>(list);
	}
	public void add(Data singleData)
	{
		data.add(singleData);
	}

	@Override
	public Iterator<Data> iterator()
	{
		return data.iterator();
	}
	
	public double[][] getRawData()
	{
		if (data.isEmpty()) return new double[0][0];
		int spectrumSize = data.get(0).getSpectrum().length;
		double[][] ret = new double[data.size()][spectrumSize];
		for (int i = 0; i < data.size(); ++i)
			for (int j = 0; j < spectrumSize; ++j)
				ret[i][j] = data.get(i).getSpectrum()[j];
		return ret;
	}
	public Data get(int i)
	{
		return data.get(i);
	}
	public int size()
	{
		return data.size();
	}
	public Collection<? extends Data> subList(int start, int end)
	{
		return data.subList(start, end);
	}
	public boolean isEmpty()
	{
		return data.isEmpty();
	}
}

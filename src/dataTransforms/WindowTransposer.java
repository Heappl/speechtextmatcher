package dataTransforms;
import java.util.ArrayList;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DoubleData;


public class WindowTransposer extends BaseDataProcessor
{
	ArrayList<DoubleData> buffer = new ArrayList<DoubleData>();
	int window = 100;
	int lastBufferIndex = 0;
	int maxBufferIndex = 0;
	
	@Override
	public Data getData() throws DataProcessingException
	{
		while (buffer.size() < window) {
			Data data = getPredecessor().getData();
			if (data == null) return null;
			if (!(data instanceof DoubleData)) continue;
			buffer.add((DoubleData)data);		
		}
		
		int maxBufferIndex = buffer.get(0).getValues().length;
		int sampleRate = buffer.get(0).getSampleRate();
		long firstSampleNumber = buffer.get(0).getFirstSampleNumber();

		double[] data = new double[window];
		for (int i = 0; i < window; ++i) {
			data[i] = buffer.get(i).getValues()[lastBufferIndex];
		}
		Data ret = new DoubleData(data, sampleRate, 0, firstSampleNumber);
		
		++lastBufferIndex;
		if (lastBufferIndex >= maxBufferIndex) {
			buffer.clear();
			lastBufferIndex = 0;
		}
		return ret;
	}
	
}

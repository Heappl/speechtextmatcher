package dataTransforms;
import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;


public class FloatDataToDoubleConverter extends BaseDataProcessor
{
	@Override
	public Data getData() throws DataProcessingException
	{
		Data data = getPredecessor().getData();
		
		if (data instanceof FloatData) {
			FloatData floatData = (FloatData) data;
			double[] values = new double[floatData.getValues().length];
			for (int i = 0; i < values.length; ++i) {
				values[i] = floatData.getValues()[i];
			}
			return new DoubleData(values, floatData.getSampleRate(), floatData.getCollectTime(), floatData.getFirstSampleNumber());
		}
		
		return data;
	}

}

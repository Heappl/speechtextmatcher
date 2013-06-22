package dataTransforms;
import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;


public class DataLogApplier  extends BaseDataProcessor
{
	@Override
	public Data getData() throws DataProcessingException
	{
		Data data = getPredecessor().getData();
		if (data instanceof FloatData) {
			FloatData fdata = (FloatData)data;
			for (int i = 0; i < fdata.getValues().length; ++i)
				fdata.getValues()[i] = (float)Math.log(fdata.getValues()[i]);
		} else if (data instanceof DoubleData) {
			DoubleData ddata = (DoubleData)data;
			for (int i = 0; i < ddata.getValues().length; ++i)
				ddata.getValues()[i] = Math.log(ddata.getValues()[i]);
		}
		return data;
	}

}

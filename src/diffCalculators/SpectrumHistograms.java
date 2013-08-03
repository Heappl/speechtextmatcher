package diffCalculators;

import common.DataSequence;
import common.statistics.Histogram;


public class SpectrumHistograms
{
	private Histogram[] histograms = null; 
	
	public SpectrumHistograms(DataSequence allData, int baskets)
	{
		int spectrumSize = allData.get(0).getSpectrum().length;
		this.histograms = new Histogram[spectrumSize];
		for (int i = 0; i < spectrumSize; ++i) {
			double[] data = new double[allData.size()];
			for (int j = 0; j < allData.size(); ++j)
				data[j] = allData.get(j).getSpectrum()[i];
			this.histograms[i] = new Histogram(data, baskets);
		}
	}
	
	public Histogram[] getHistograms()
	{
		return this.histograms;
	}
}

package diffCalculators;

import common.DataSequence;


public class SpectrumWeights
{
	private double[] weights;

	public SpectrumWeights(DataSequence allData)
	{
		int spectrumSize = allData.get(0).getSpectrum().length;
		SpectrumHistograms histograms = new SpectrumHistograms(allData, 100);
		double[] spectrumFromNormalDiff = new double[spectrumSize];
		double max = 0;
		for (int i = 0; i < spectrumSize; ++i) {
			spectrumFromNormalDiff[i] = histograms.getHistograms()[i].differenceFromNormalDistribution();
			if (spectrumFromNormalDiff[i] > max) max = spectrumFromNormalDiff[i];
		}
		weights = new double[spectrumSize];
		for (int i = 0; i < spectrumSize; ++i)
			weights[i] = 10 * spectrumFromNormalDiff[i] / max;
	}

	public double[] getWeights() {
		return this.weights;
	}
	
}

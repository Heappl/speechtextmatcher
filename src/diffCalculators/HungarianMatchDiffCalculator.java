package diffCalculators;
import common.Data;

import algorithms.HungarianAlgorithm;


public class HungarianMatchDiffCalculator implements ISequenceDiffCalculator
{
	private ISpectrumDiffCalculator diffCalculator = new SpectrumDiffCalculator();

	public double diff(Data[] first, Data[] second)
	{
		if ((first.length == 0) || (second.length == 0)) return Double.MAX_VALUE;
		double[][] weights = new double[first.length][second.length];
		for (int i = 2; i < first.length - 2; ++i) {
			for (int j = 2; j < second.length - 2; ++j) {
				weights[i][j] = diffCalculator.diff(first[i].getSpectrum(), second[j].getSpectrum());
				weights[i][j] += diffCalculator.diff(first[i - 1].getSpectrum(), second[j - 1].getSpectrum()) / 2.0;
				weights[i][j] += diffCalculator.diff(first[i + 1].getSpectrum(), second[j + 1].getSpectrum()) / 2.0;
				weights[i][j] += diffCalculator.diff(first[i - 2].getSpectrum(), second[j - 2].getSpectrum()) / 4.0;
				weights[i][j] += diffCalculator.diff(first[i + 2].getSpectrum(), second[j + 2].getSpectrum()) / 4.0;
			}
		}
		HungarianAlgorithm ha = new HungarianAlgorithm(weights);
		int[] matching = ha.execute();
		double score = 0;
		for (int i = 0; i < matching.length; ++i)
			score += weights[i][matching[i]];
		return score;
	}
}

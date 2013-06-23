package diffCalculators;
import common.Data;

import algorithms.HungarianAlgorithm;


public class HungarianMatchDiffCalculator implements ISequenceDiffCalculator
{
	private ISpectrumDiffCalculator diffCalculator = new SpectrumDiffCalculator();
	
	public HungarianMatchDiffCalculator(ISpectrumDiffCalculator diffCalculator)
	{
		this.diffCalculator = diffCalculator;
	}

	public double diff(Data[] first, Data[] second)
	{
		if ((first.length == 0) || (second.length == 0)) return Double.MAX_VALUE;
		double[][] weights = calculateDiffs(first, second);
		HungarianAlgorithm ha = new HungarianAlgorithm(weights);
		int[] matching = ha.execute();
		double score = 0;
		for (int i = 0; i < matching.length; ++i)
			score += weights[i][matching[i]];
		return score;
	}
	
	private double[][] calculateDiffs(Data[] first, Data[] second)
	{
		int window = 5;
		double divisor = 2;
		double[][] weights = new double[first.length - 2 * window][second.length - 2 * window];
		for (int i = window; i < first.length - window; ++i) {
			for (int j = window; j < second.length - window; ++j) {
				for (int k = -window; k <= window; ++k) {
					double currDivisor = (k == j) ? 1.0 : Math.pow(divisor, Math.abs(j - k));
					double[] firstSpectrum = first[i + k].getSpectrum();
					double[] secondSpectrum = second[j + k].getSpectrum();
					double diff = diffCalculator.diff(firstSpectrum, secondSpectrum);
					weights[i - window][j - window] += diff / currDivisor;
				}
			}
		}
		return weights;
	}
}

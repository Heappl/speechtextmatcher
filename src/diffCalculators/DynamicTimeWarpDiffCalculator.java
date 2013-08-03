package diffCalculators;

import common.Data;
import common.DataSequence;




public class DynamicTimeWarpDiffCalculator implements ISequenceDiffCalculator
{
//	private SpectrumMahalanobisDiffCalculator frameDiffCalculator;
	private SpectrumDiffCalculator frameDiffCalculator;
	
	public DynamicTimeWarpDiffCalculator(DataSequence allData)
	{
//		this.frameDiffCalculator = new SpectrumMahalanobisDiffCalculator(allData);
		this.frameDiffCalculator = new SpectrumDiffCalculator();
	}
	
	public double diff(Data[] first, Data[] second)
	{
		if ((first.length == 0) || (second.length == 0)) return Double.MAX_VALUE;
		if (first.length < second.length) return diff(second, first);
		
		double[] partial = new double[first.length];
		for (int i = 0; i < first.length; ++i)
			partial[i] = ((i > 0) ? partial[i - 1] : 0) + calculateDiff(first[i], second[0]);
		
		double ret = Double.MAX_VALUE;
		for (int i = 0; i < second.length; ++i)
		{
			if (partial[partial.length - 1] < ret) {
				ret = partial[partial.length - 1];
			}
			double[] next = new double[partial.length];
			for (int j = 0; j < first.length; ++j) {
				double horizontal = partial[j] + ((i > 0) ? calculateDiff(second[i - 1], first[j]) : 0);
				double vertical = (j > 0) ? (next[j - 1] + calculateDiff(second[i], first[j - 1])) : Double.MAX_VALUE;
				double diagonal = ((j > 0) ? partial[j - 1] : 0) + calculateDiff(first[j], second[i]);
				next[j] = Math.min(diagonal, Math.min(horizontal, vertical));
			}
			partial = next;
		}

		for (int  i = 0; i < partial.length; ++i) {
			if (partial[i] < ret) {
				ret = partial[i];
			}
		}
		return ret;
	}
	
	double calculateDiff(Data first, Data second)
	{
		return frameDiffCalculator.diffNorm2(first.getSpectrum(), second.getSpectrum());
	}
}

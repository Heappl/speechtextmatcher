
public class SpectrumDiffCalculator
{
	private double[] weights = null;
	
	public SpectrumDiffCalculator()
	{
	}
	public SpectrumDiffCalculator(double[] weights)
	{
		this.weights = weights;
	}
	
	double diffNorm1(double[] first, double[] second)
	{
		double res = 0;
		for (int i = 0; i < first.length; ++i) {
			double weight = (weights == null) ? 1.0 : weights[i];
			res += Math.abs(first[i] - second[i]) * weight;
		}
		return res;
	}
	
	double diffNorm2(double[] first, double[] second)
	{
		double res = 0;
		for (int i = 0; i < first.length; ++i) {
			double weight = (weights == null) ? 1.0 : weights[i];
			res += (first[i] - second[i]) * (first[i] - second[i]) * weight;
		}
		return res;
	}
	
	double diffNormInf(double[] first, double[] second)
	{
		double res = 0;
		for (int i = 0; i < first.length; ++i) {
			double weight = (weights == null) ? 1.0 : weights[i];
			res = Math.max(res, (first[i] - second[i]) * weight);
		}
		return res;
	}
}

package common.statistics;

import common.algorithms.gaussian.NormalDistribution;


public class Histogram
{
	private double max = Double.NEGATIVE_INFINITY;
	private double min = Double.POSITIVE_INFINITY;
	private double step = 0;
	private double average = 0;
	private double deviation = 0;
	private int[] counts = null;
	private int numberOfTries;

	public Histogram(double[] data, int baskets)
	{
		counts = new int[baskets];
		for (double val : data) {
			if (val < min) min = val;
			else if (val > max) max = val;
			average += val;
		}
		average /= data.length;
		step = (max - min) / baskets;
		for (double val : data) {
			int basket = Math.min(baskets - 1, (int)Math.floor((val - min) / step));
			counts[basket]++;
			deviation += Math.abs(val - average);
		}
		deviation /= data.length;
		numberOfTries = data.length;
	}
	
	public double differenceFromNormalDistribution()
	{
		NormalDistribution normalDistribution = new NormalDistribution(average, deviation);
		double diff = 0;
		for (int i = 0; i < counts.length; ++i) {
			double expected =
				normalDistribution.getExpectedValues(i * step + min, (i + 1) * step + min, numberOfTries);
			diff += (expected - counts[i]) * (expected - counts[i]);
		}
		return diff;
	}
}

package common.algorithms;

public class KMeans {
	private double[][] means;
	private int[] classification;
	private final static int SEED = 179424673;
	
	public KMeans(double[][] data, int k) {
		this.classification = new int[data.length];
		this.means = solve(data, k);
	}
	
	public double[] getMean(int i)
	{
		return this.means[i];
	}
	
	public int getClassification(int i)
	{
		return this.classification[i];
	}
	
	private double[][] solve(double[][] data, int k)
	{
		if (data.length == 0) return new double[k][0];
		double[][] means = new double[k][data[0].length];
		
		for (int i = 0; i < k; ++i)
			for (int j = 0; j < data[i].length; ++j)
				means[i][j] = data[(int)((long)i * (long)SEED % (long)data.length)][j];
		
		int i = 0;
		while (classify(data, means))
		{
			System.err.println("it " + i++);
			means = recalculateMeans(data, k);
		}
		return means;
	}

	private double[][] recalculateMeans(double[][] data, int k)
	{
		double[][] ret = new double[k][data[0].length];
		int[] counts = new int[k];
		for (int i = 0; i < data.length; ++i)
		{
			for (int j = 0; j < data[i].length; ++j)
				ret[classification[i]][j] += data[i][j];
			counts[classification[i]]++;
		}
		for (int i = 0; i < k; ++i)
			for (int j = 0; j < ret[i].length; ++j)
				ret[i][j] /= (double)counts[i];
		return ret;
	}

	private boolean classify(double[][] data, double[][] means)
	{
		boolean changed = false;
		for (int j = 0; j < data.length; ++j)
		{
			double nearestDiff = Double.MAX_VALUE;
			int nearestInd = 0;
			for (int i = 0; i < means.length; ++i)
			{
				double diff = diff(data[j], means[i]);
				if (diff < nearestDiff)
				{
					nearestDiff = diff;
					nearestInd = i;
				}
			}
			changed |= (classification[j] != nearestInd);
			classification[j] = nearestInd;
		}
		return changed;
	}

	private double diff(double[] val, double[] d)
	{
		double ret = 0;
		for (int i = 0; i < val.length; ++i)
			ret += (val[i] - d[i]) * (val[i] - d[i]);
		return ret;
	}
}

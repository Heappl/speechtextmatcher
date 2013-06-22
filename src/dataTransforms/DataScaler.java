package dataTransforms;

public class DataScaler
{
	private class MinAndMax
	{
		private double min = Double.MAX_VALUE;
		private double max = Double.MIN_VALUE;
		
		MinAndMax withNewValue(double value)
		{
			if (min > value) this.min = value;
			if (max < value) this.max = value;
			return this;
		}
		MinAndMax withNewValue(MinAndMax value)
		{
			if (min > value.min) this.min = value.min;
			if (max < value.max) this.max = value.max;
			return this;
		}
	};
	
	public int[] scale(double[] data, int minValue, int maxValue)
	{
		int[] ret = new int[data.length];
		MinAndMax minAndMax = findMinAndMax(data);
		double scaleFactor = getScaleFactor(minAndMax, minValue, maxValue);
		for (int i = 0; i < data.length; ++i)
			ret[i] = (int)Math.floor(data[i] / scaleFactor);
		return ret;
	}
	public int[][] scale(double[][] data, int minValue, int maxValue)
	{
		if (data.length == 0) return new int[0][0];
		int[][] ret = new int[data.length][data[0].length];
		MinAndMax minAndMax = findMinAndMax(data);
		double scaleFactor = getScaleFactor(minAndMax, minValue, maxValue);
		System.err.println("scale min:" + minAndMax.min + " max:" + minAndMax.max + " factor:" + scaleFactor);
		for (int i = 0; i < data.length; ++i)
			for (int j = 0; j < data[i].length; ++j)
				ret[i][j] = (int)Math.floor((data[i][j] - minAndMax.min) / scaleFactor) + minValue;
		return ret;
	}

	private double getScaleFactor(MinAndMax actualMinAndMax, int requestedMin, int requestedMax)
	{
		double actualSpread = (actualMinAndMax.max - actualMinAndMax.min) * 1.0000001;
		int requestedSpread = requestedMax - requestedMin;
		return actualSpread / requestedSpread;
	}
	
	private MinAndMax findMinAndMax(double[][] data)
	{
		MinAndMax ret = new MinAndMax();
		for (double[] row : data) ret = ret.withNewValue(findMinAndMax(row));
		return ret;
	}
	private MinAndMax findMinAndMax(double[] data)
	{
		MinAndMax ret = new MinAndMax();
		for (double value : data) ret = ret.withNewValue(value);
		return ret;
	}
}

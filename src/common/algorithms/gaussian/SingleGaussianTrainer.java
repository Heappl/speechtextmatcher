package common.algorithms.gaussian;



public class SingleGaussianTrainer
{
    public MultivariateNormalDistribution train(double[][] data)
    {
        double[] mean = calculateMean(data);
        double[][] covariances = calculateCovariances(data, mean);
        
        return new MultivariateNormalDistribution(mean, covariances);
    }

    private double[][] calculateCovariances(double[][] data, double[] mean)
    {
        int d = data[0].length;
        double[][] out = new double[d][d];
        for (int i = 0; i < data.length; ++i)
            for (int j = 0; j < d; ++j)
                for (int k = 0; k < d; ++k)
                    out[j][k] += (data[i][j] - mean[j]) * (data[i][k] - mean[k]);
        for (int j = 0; j < d; ++j)
            for (int k = 0; k < d; ++k)
                out[j][k] /= data.length;
        return out;
    }

    private double[] calculateMean(double[][] data)
    {
        double[] out = new double[data[0].length];
        
        for (int i = 0; i < data.length; ++i)
            for (int j = 0; j < data[i].length; ++j)
                out[j] += data[i][j];
        for (int j = 0; j < out.length; ++j)
            out[j] /= data.length;
        
        return out;
    }
}

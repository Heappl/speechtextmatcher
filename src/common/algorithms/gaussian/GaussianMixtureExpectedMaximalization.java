package common.algorithms.gaussian;

import common.exceptions.ImplementationError;

public class GaussianMixtureExpectedMaximalization
{
    private static final long PRIME = 2543568463L;
    
    public GaussianMixtureExpectedMaximalization()
    {
    }
    
    public MultivariateNormalDistribution[] calculate(double[][] data, int numOfModels) throws ImplementationError
    {
        int s = data.length;
        MultivariateNormalDistribution[] models = new MultivariateNormalDistribution[numOfModels];
        double[][] prevMeans;
        {
            double[][] initialProbs = new double[data.length][models.length];
            for (int i = 0; i < data.length; ++i)
                for (int j = 0; j < models.length; ++j)
                    initialProbs[i][j] = (double)1 / (double)models.length;
            double[][] initialMeans = new double[numOfModels][];
            for (int i = 0; i < numOfModels; ++i) {
                int index = (int)((PRIME * (long)i) % (long)s);
                initialMeans[i] = data[index];
            }
            double[][][] initialCovariances = calculateCovariances(data, initialMeans, initialProbs);
            for (int i = 0; i < numOfModels; ++i)
                models[i] = new MultivariateNormalDistribution(initialMeans[i], initialCovariances[i]);
            prevMeans = initialMeans;
        }
        
        int count = 1;
        while (true) {
//            System.err.println("EM iteration " + count++);
            
            double[][] probs = calculateProbabilities(data, models);
            
            double[][] newMeans = calculateMeans(data, models, probs);
            double[][][] newCovariances = calculateCovariances(data, newMeans, probs);
            for (int i = 0; i < numOfModels; ++i)
                models[i] = new MultivariateNormalDistribution(newMeans[i], newCovariances[i]);
            
            if (!changed(prevMeans, newMeans, 0.000001)) break;
            prevMeans = newMeans;
        }
        return models;
    }

    private double[][] calculateProbabilities(double[][] data, MultivariateNormalDistribution[] models) throws ImplementationError
    {
        double[][] probs = new double[data.length][models.length];
        double[] sums = new double[models.length];
        for (int i = 0; i < data.length; ++i) {
            double sum = 0;
            for (int j = 0; j < models.length; ++j) {
                double prob = Math.pow(Math.E, models[j].logLikelihood(data[i]));
                probs[i][j] = prob;
                sum += prob;
            }
            for (int j = 0; j < models.length; ++j)
                probs[i][j] /= sum;
            for (int j = 0; j < models.length; ++j)
                sums[j] += probs[i][j];
        }
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < models.length; ++j)
                probs[i][j] /= sums[j];
        }
        return probs;
    }

    private double[][] calculateMeans(
        double[][] data,
        MultivariateNormalDistribution[] models,
        double[][] probs) throws ImplementationError
    {
        int d = data[0].length;
        double[][] means = new double[models.length][d];
        
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < models.length; ++j) {
                for (int k = 0; k < d; ++k)
                    means[j][k] += probs[i][j] * data[i][k]; 
            }
        }
        return means;
    }

    private boolean changed(double[][] prev, double[][] curr, double diff)
    {
        for (int i = 0; i < prev.length; ++i) {
            for (int j = 0; j < prev[i].length; ++j) {
                if (Math.abs(prev[i][j] - curr[i][j]) > diff)
                    return true;
            }
        }
        return false;
    }

    private double[][][] calculateCovariances(double[][] data, double[][] means, double[][] probs)
    {
        int d = data[0].length;
        int numOfModels = means.length;
        double[][][] ret = new double[numOfModels][d][d];
        
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < d; ++j) {
                for (int k = 0; k < d; ++k) {
                    for (int model = 0; model < numOfModels; ++model) {
                        ret[model][j][k] +=
                            probs[i][model] * (data[i][j] - means[model][j]) * (data[i][k] - means[model][k]);
                    }
                }
            }
        }
        return ret;
    }
}

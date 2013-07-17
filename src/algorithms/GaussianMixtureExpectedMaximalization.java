package algorithms;

import commonExceptions.ImplementationError;

public class GaussianMixtureExpectedMaximalization
{
    public GaussianMixtureExpectedMaximalization()
    {
    }
    
    public MultivariateNormalDistribution[] calculate(double[][] data, int numOfModels) throws ImplementationError
    {
        int s = data.length;
        int[] classification = new int[s];
        for (int i = 0; i < s; ++i) classification[i] = i % numOfModels;
        MultivariateNormalDistribution[] models;
        
        int count = 1;
        while (true) {
            System.err.println("iteration " + count++);
            models = calculateModels(data, classification, numOfModels);
            
            for (int i = 0; i < models.length; ++i) {
                System.err.println("  " + models[i]);
            }
            
            int[] newClassification = reclassify(data, models, classification);
            if (!changed(newClassification, classification)) break;
            classification = newClassification;
        }
        return models;
    }

    private boolean changed(int[] newClassification, int[] classification)
    {
        for (int i = 0; i < classification.length; ++i)
            if (classification[i] != newClassification[i])
                return true;
        return false;
    }

    private int[] reclassify(double[][] data, MultivariateNormalDistribution[] models, int[] classification) throws ImplementationError
    {
        int[] ret = new int[classification.length];
        for (int i = 0; i < data.length; ++i) {
            int mostProbableModel = 0;
            double bestModelLikelihood = Double.MIN_VALUE;
            for (int j = 0; j < models.length; ++j) {
                double logLikelihood = models[j].logLikelihood(data[i]);
                if ((bestModelLikelihood == Double.MIN_VALUE) || (logLikelihood > bestModelLikelihood)) {
                    bestModelLikelihood = logLikelihood;
                    mostProbableModel = j;
                }
            }
            ret[i] = mostProbableModel;
        }
        return ret;
    }

    private MultivariateNormalDistribution[] calculateModels(double[][] data, int[] classification, int numOfModels)
    {
        double[][] means = calculateMeans(data, classification, numOfModels);
        double[][][] covariances = calculateCovariances(data, means, classification, numOfModels);
        MultivariateNormalDistribution[] models = new MultivariateNormalDistribution[numOfModels];
        for (int i = 0; i < numOfModels; ++i)
            models[i] = new MultivariateNormalDistribution(means[i], covariances[i]);
        return models;
    }

    private double[][][] calculateCovariances(double[][] data, double[][] mean, int[] classification, int numOfModels)
    {
        int d = data[0].length;
        double[][][] ret = new double[numOfModels][d][d];
        int[] counts = new int[numOfModels];
        
        for (int i = 0; i < data.length; ++i) {
            int model = classification[i];
            for (int j = 0; j < d; ++j) {
                for (int k = 0; k < d; ++k) {
                    ret[model][j][k] += (data[i][j] - mean[model][j]) * (data[i][k] - mean[model][k]);
                }
            }
            counts[model]++;
        }
        for (int i = 0; i < numOfModels; ++i) {
            for (int j = 0; j < d; ++j) {
                for (int k = 0; k < d; ++k) {
                    ret[i][j][k] /= counts[i];
                }
            }
        }
        return ret;
    }

    private double[][] calculateMeans(double[][] data, int[] classification, int numOfModels)
    {
        int d = data[0].length;
        double[][] ret = new double[numOfModels][d];
        int[] counts = new int[numOfModels];
        
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < d; ++j) {
                ret[classification[i]][j] += data[i][j];
            }
            counts[classification[i]]++;
        }
        for (int i = 0; i < numOfModels; ++i) {
            for (int j = 0; j < d; ++j) {
                ret[i][j] /= counts[i];
            }
        }
        return ret;
    }
}

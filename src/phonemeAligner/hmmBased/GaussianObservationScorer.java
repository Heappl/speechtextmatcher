package phonemeAligner.hmmBased;

import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.algorithms.hmm.ITrainableObservationLogLikelihoodCalculator;
import common.algorithms.hmm.LogMath;

public class GaussianObservationScorer
    implements ITrainableObservationLogLikelihoodCalculator
{
    
    private boolean startedTraining = false;
    private double[] mean;
    private double[][] covariances;
    private MultivariateNormalDistribution distribution = null;
    
    @Override
    public void addObservation(double[] observation, float normalizedLogLikelihood)
    {
        if (!startedTraining) startTraining(observation.length);
        double probability = LogMath.logToLinear(normalizedLogLikelihood);
        for (int i = 0; i < mean.length; ++i)
            this.mean[i] = observation[i] * probability;
    }

    @Override
    public void addObservationAgain(double[] observation, float normalizedLogLikelihood)
    {
        double probability = LogMath.logToLinear(normalizedLogLikelihood);
        for (int i = 0; i < this.mean.length; ++i) {
            for (int j = 0; j < this.mean.length; ++j) {
                this.covariances[i][j] +=
                    probability * (observation[i] - this.mean[i]) * (observation[j] - this.mean[j]);
            }
        }
    }

    @Override
    public void finishTraining()
    {
        this.distribution = new MultivariateNormalDistribution(this.mean, this.covariances);
        this.startedTraining = false;
        this.mean = null;
        this.covariances = null;
    }

    private void startTraining(int numOfDimensions)
    {
        this.mean = new double[numOfDimensions];
        this.covariances = new double[numOfDimensions][numOfDimensions];
        this.startedTraining = true;
    }

    @Override
    public float observationLogLikelihood(double[] observation)
    {
        if (this.distribution == null) return Float.POSITIVE_INFINITY;
        return (float)Math.min(
                    Float.MAX_VALUE,
                    Math.max(-Float.MAX_VALUE, this.distribution.logLikelihood(observation)));
    }
}

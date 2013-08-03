package phonemeAligner.hmmBased;

import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.algorithms.hmm.ITrainableObservationLogLikelihoodCalculator;
import common.algorithms.hmm.LogMath;

public class GaussianObservationScorer
    implements ITrainableObservationLogLikelihoodCalculator
{
    
    private boolean startedTraining = false;
    private boolean startedSecondPhase = false;
    private double[] mean;
    private double totalProbability = 0;
    private float totalLogLikelihood = 0;
    private double[][] covariances;
    private MultivariateNormalDistribution distribution = null;
    
    @Override
    public void addObservation(double[] observation, float normalizedLogLikelihood)
    {
        if (!startedTraining) startTraining(observation.length);
        double probability = LogMath.logToLinear(normalizedLogLikelihood);
        for (int i = 0; i < mean.length; ++i)
            this.mean[i] = observation[i] * probability;
        this.totalProbability += probability;
    }

    @Override
    public void addObservationAgain(double[] observation, float normalizedLogLikelihood)
    {
        if (!startedSecondPhase) initiateSecondPhase();
        double probability = LogMath.logToLinear(normalizedLogLikelihood - this.totalLogLikelihood);
        for (int i = 0; i < this.mean.length; ++i) {
            for (int j = 0; j < this.mean.length; ++j) {
                this.covariances[i][j] +=
                    probability * (observation[i] - this.mean[i]) * (observation[j] - this.mean[j]);
            }
        }
    }

    private void initiateSecondPhase()
    {
        for (int i = 0; i < this.mean.length; ++i)
            this.mean[i] /= this.totalProbability;
        this.totalLogLikelihood = LogMath.linearToLog(this.totalProbability);
        this.startedSecondPhase = true;
    }

    @Override
    public void finishTraining()
    {
        this.distribution = new MultivariateNormalDistribution(this.mean, this.covariances);
        this.startedTraining = false;
        this.startedSecondPhase = false;
        this.mean = null;
        this.covariances = null;
    }

    private void startTraining(int numOfDimensions)
    {
        this.mean = new double[numOfDimensions];
        this.covariances = new double[numOfDimensions][numOfDimensions];
        this.startedTraining = true;
        this.totalProbability = 0;
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

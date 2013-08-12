package phonemeAligner.hmmBased;

import common.LogMath;
import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.algorithms.hmm.ITrainableObservationLogLikelihoodCalculator;
import common.exceptions.ImplementationError;

public class GaussianObservationScorer
    implements ITrainableObservationLogLikelihoodCalculator
{
    
    private boolean startedTraining = false;
    private double[] mean;
    private float totalLogLikelihood = Float.POSITIVE_INFINITY;
    private double[][] covariances;
    private MultivariateNormalDistribution distribution = null;
    private int count1 = 0;
    private int count2 = 0;
    private int totalCount = 0;
    
    @Override
    public void addObservation(double[] observation, float logLikelihood)
    {
        if (!startedTraining) startTraining(observation.length);
        logLikelihood = Math.min(0, logLikelihood);
        
        float nextTotal = (this.totalLogLikelihood == Float.POSITIVE_INFINITY) ? logLikelihood :
            LogMath.logAdd(this.totalLogLikelihood, logLikelihood);
        float previousLikelihood =
                ((this.totalLogLikelihood == Float.POSITIVE_INFINITY) ? 0 :
                    this.totalLogLikelihood - nextTotal);
        float observationLikelihood = logLikelihood - nextTotal;
        
        double previousProbability = LogMath.logToLinear(previousLikelihood);
        double observationProbability = LogMath.logToLinear(observationLikelihood);
        
        if (observationProbability == 0) this.count1++;
        
        if ((!(observationProbability > Float.NEGATIVE_INFINITY))
            || (!(previousProbability > Float.NEGATIVE_INFINITY)))
            throw new ImplementationError(
                    logLikelihood + " "
                    + this.totalLogLikelihood + " "
                    + nextTotal + " "
                    + previousLikelihood + " "
                    + previousProbability + " "
                    + observationLikelihood + " "
                    + observationProbability);
        for (int i = 0; i < mean.length; ++i) {
            this.mean[i] *= previousProbability;
            this.mean[i] += observation[i] * observationProbability;

            if (!(this.mean[i] > Float.NEGATIVE_INFINITY))
                throw new ImplementationError(
                        this.mean[i] + " " + logLikelihood + " "
                        + this.totalLogLikelihood + " "
                        + nextTotal + " "
                        + previousLikelihood + " "
                        + previousProbability + " "
                        + observationLikelihood + " "
                        + observationProbability);
        }
        this.totalLogLikelihood = nextTotal;
        this.totalCount++;
    }

    @Override
    public void addObservationAgain(double[] observation, float logLikelihood)
    {
        if (logLikelihood == Float.NEGATIVE_INFINITY) return;
        logLikelihood = Math.min(0, logLikelihood);
        float observationLikelihood = logLikelihood - this.totalLogLikelihood;
        double probability = LogMath.logToLinear(observationLikelihood);
        if (probability == 0) {
            this.count2++;
//            if (this.count2 * 2 > this.totalCount)
//            System.err.println(this.count2 + " " + this.totalCount + " " + logLikelihood + " "
//                    + observationLikelihood + " " + this.totalLogLikelihood + " " + probability);
        }
        for (int i = 0; i < this.mean.length; ++i) {
            for (int j = 0; j < this.mean.length; ++j) {
                this.covariances[i][j] +=
                    probability * (observation[i] - this.mean[i]) * (observation[j] - this.mean[j]);
                if (!(this.covariances[i][j] > Float.NEGATIVE_INFINITY)) {
                    throw new ImplementationError(
                            this.covariances[i][j] + " " + logLikelihood + " " + probability + " "
                            + observationLikelihood + " " + this.totalLogLikelihood
                            );
                }
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
        this.totalCount = 0;
    }

    private void startTraining(int numOfDimensions)
    {
        this.totalCount = 0;
        this.mean = new double[numOfDimensions];
        this.covariances = new double[numOfDimensions][numOfDimensions];
        this.totalLogLikelihood = Float.POSITIVE_INFINITY;
        this.startedTraining = true;
    }

    @Override
    public float observationLogLikelihood(double[] observation)
    {
        if (this.distribution == null) return Float.NaN;
        return (float)Math.min(
                    Float.MAX_VALUE,
                    Math.max(-Float.MAX_VALUE, this.distribution.logLikelihood(observation)));
    }
}

package common.algorithms.hmm2;

public interface ITrainableObservationLogLikelihoodCalculator
{
    public void addObservation(double[] observation, double logLikelihood);
    public void finishTraining();
    public void startTraining();
}

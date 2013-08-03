package common.algorithms.hmm;

public interface ITrainableObservationLogLikelihoodCalculator
{
    public void addObservation(double[] observation, float normalizedLogLikelihood);
    public void finishTraining();
    public void addObservationAgain(double[] observation, float normalizedLogLikelihood);
    public float observationLogLikelihood(double[] observation);
}

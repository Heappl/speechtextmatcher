package phonemeAligner.hmmBased;

import common.algorithms.hmm2.ITrainableObservationLogLikelihoodCalculator;

public class GaussianObservationScorer
    implements ITrainableObservationLogLikelihoodCalculator
{
    public GaussianObservationScorer()
    {
    }

    @Override
    public void addObservation(double[] observation, double logLikelihood)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void finishTraining()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void startTraining()
    {
        // TODO Auto-generated method stub
    }
}

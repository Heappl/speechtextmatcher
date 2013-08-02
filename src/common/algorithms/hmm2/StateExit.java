package common.algorithms.hmm2;

public class StateExit
{
    private double logLikelihood;

    public void updateLikelihood(double newLogLikelihood)
    {
        this.logLikelihood = newLogLikelihood;
    }
}

package common.algorithms.hmm;

public class StateExit
{
    private double logLikelihood;

    public void updateLikelihood(double newLogLikelihood)
    {
        this.logLikelihood = newLogLikelihood;
    }
}

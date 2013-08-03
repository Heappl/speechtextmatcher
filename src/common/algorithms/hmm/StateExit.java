package common.algorithms.hmm;

public class StateExit
{
    private float logLikelihood;

    public void updateLikelihood(float newLogLikelihood)
    {
        this.logLikelihood = newLogLikelihood;
    }

    public float getLogLikelihood()
    {
        return this.logLikelihood;
    }
}

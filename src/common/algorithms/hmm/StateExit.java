package common.algorithms.hmm;

public class StateExit
{
    private float logLikelihood = 0;

    public void updateLikelihood(float newLogLikelihood)
    {
        this.logLikelihood = newLogLikelihood;
    }

    public float getLogLikelihood()
    {
        return this.logLikelihood;
    }
}

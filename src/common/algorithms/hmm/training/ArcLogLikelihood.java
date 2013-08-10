package common.algorithms.hmm.training;

import common.algorithms.hmm.Arc;

public class ArcLogLikelihood
{
    private final Arc arc;
    private final float logLikelihood;
    
    public ArcLogLikelihood(Arc arc, float logLikelihood)
    {
        this.arc = arc;
        this.logLikelihood = logLikelihood;
    }
    public Arc getArc()
    {
        return this.arc;
    }
    public float getLogLikelihood()
    {
        return this.logLikelihood;
    }
}

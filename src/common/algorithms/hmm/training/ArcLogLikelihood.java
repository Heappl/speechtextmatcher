package common.algorithms.hmm.training;

import common.algorithms.hmm.Arc;

public class ArcLogLikelihood
{
    private final Arc arc;
    private final float logLikelihood;
    private final double[] nextObservation;
    
    public ArcLogLikelihood(Arc arc, float logLikelihood, double[] nextObservation)
    {
        this.arc = arc;
        this.logLikelihood = logLikelihood;
        this.nextObservation = nextObservation;
    }
    public Arc getArc()
    {
        return this.arc;
    }
    public float getLogLikelihood()
    {
        return this.logLikelihood;
    }
    public double[] getNextObservation()
    {
        return this.nextObservation;
    }
}

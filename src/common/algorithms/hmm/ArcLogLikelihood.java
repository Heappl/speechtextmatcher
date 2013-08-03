package common.algorithms.hmm;

public class ArcLogLikelihood
{
    private final Arc arc;
    private final double logLikelihood;
    
    public ArcLogLikelihood(Arc arc, double logLikelihood)
    {
        this.arc = arc;
        this.logLikelihood = logLikelihood;
    }
    
    public Arc getArc()
    {
        return this.arc;
    }

    public double getLogLikelihood()
    {
        return this.logLikelihood;
    }

}

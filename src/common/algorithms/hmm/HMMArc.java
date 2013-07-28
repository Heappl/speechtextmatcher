package common.algorithms.hmm;

public class HMMArc
{
    private HMMState incomingTo;
    private double logLikelihood;
    
    public HMMArc(HMMState incomingTo, double transitionLogLikelihood)
    {
        this.incomingTo = incomingTo;
        this.logLikelihood = transitionLogLikelihood;
    }
    public HMMArc(HMMState incomingTo)
    {
        this.incomingTo = incomingTo;
        this.logLikelihood = 0;
    }
    
    public HMMState getDestination()
    {
        return this.incomingTo;
    }
    
    public double getLogLikelihood()
    {
        return this.logLikelihood;
    }
}

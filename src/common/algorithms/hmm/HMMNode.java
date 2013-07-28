package common.algorithms.hmm;

public class HMMNode
{
    private double[] transitionsLogLikelihoods;
    
    public HMMNode(double[] transitionsLogLikelihoods)
    {
        this.transitionsLogLikelihoods = transitionsLogLikelihoods;
    }
    public int getNumOfArcs()
    {
        return this.transitionsLogLikelihoods.length;
    }
}

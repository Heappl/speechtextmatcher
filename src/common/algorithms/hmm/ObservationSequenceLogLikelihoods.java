package common.algorithms.hmm;

import java.util.ArrayList;
import java.util.Iterator;

public class ObservationSequenceLogLikelihoods implements Iterable<NodeLogLikelihoods>
{
    private final double logLikelihood;
    private final ArrayList<NodeLogLikelihoods> nodesLogLikelihoods;
    
    public ObservationSequenceLogLikelihoods(double logLikelihood,
                                             ArrayList<NodeLogLikelihoods> nodesLogLikelihoods)
    {
        this.logLikelihood = logLikelihood;
        this.nodesLogLikelihoods = nodesLogLikelihoods;
    }

    @Override
    public Iterator<NodeLogLikelihoods> iterator()
    {
        return this.nodesLogLikelihoods.iterator();
    }

    public double getLogLikelihood()
    {
        return this.logLikelihood;
    }
}

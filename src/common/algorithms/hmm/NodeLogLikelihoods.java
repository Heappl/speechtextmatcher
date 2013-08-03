package common.algorithms.hmm;

import java.util.ArrayList;
import java.util.Iterator;

public class NodeLogLikelihoods implements Iterable<ArcLogLikelihood>
{
    private final Node node;
    private final double logLikelihood;
    private final double[] observation;
    private final ArrayList<ArcLogLikelihood> arcLikelihoods;
    
    public NodeLogLikelihoods(
        Node node,
        double[] observation,
        double logLikelihood,
        ArrayList<ArcLogLikelihood> arcLikelihoods)
    {
        this.node = node;
        this.logLikelihood = logLikelihood;
        this.arcLikelihoods = arcLikelihoods;
        this.observation = observation;
    }
    
    public Node getNode()
    {
        return this.node;
    }

    @Override
    public Iterator<ArcLogLikelihood> iterator()
    {
        return this.arcLikelihoods.iterator();
    }

    public double[] getObservation()
    {
        return this.observation;
    }

    public double getLogLikelihood()
    {
        return this.logLikelihood;
    }
}

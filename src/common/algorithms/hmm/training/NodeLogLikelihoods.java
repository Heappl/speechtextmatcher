package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.Iterator;

import common.algorithms.hmm.Node;

public class NodeLogLikelihoods implements Iterable<ArcLogLikelihood>
{
    private final Node node;
    private final float logLikelihood;
    private final float logLikelihoodWithoutObservation;
    private final double[] observation;
    private final double[] nextObservation;
    private final ArrayList<ArcLogLikelihood> arcLikelihoods;
    
    public NodeLogLikelihoods(
        Node node,
        double[] observation,
        double[] nextObservation,
        float logLikelihood,
        float logLikelihoodWithoutObservation,
        ArrayList<ArcLogLikelihood> incomingArcLikelihoods)
    {
        this.node = node;
        this.logLikelihood = logLikelihood;
        this.logLikelihoodWithoutObservation = logLikelihoodWithoutObservation;
        this.arcLikelihoods = incomingArcLikelihoods;
        this.observation = observation;
        this.nextObservation = nextObservation;
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

    public double[] getNextObservation()
    {
        return this.nextObservation;
    }

    public float getLogLikelihood()
    {
        return this.logLikelihood;
    }
    
    public float getLogLikelihoodWithoutObservation()
    {
        return this.logLikelihoodWithoutObservation;
    }
    
    public String toString()
    {
        return "[" + this.node.getName() + " " + this.logLikelihood + "]";
    }
}

package common.algorithms.hmm.training;

import java.util.ArrayList;

import common.algorithms.hmm.Node;

public class NodeLogLikelihoodsCalculator
{
    public ObservationSequenceLogLikelihoods calculate(
        double[][] observationSequence,
        Node possibleModel)
    {
        
        ObservationSequenceLogLikelihoods backwardLikelihoods =
                new SequenceScorer().scoreForSequence(
                        observationSequence, createBackwardScorers(possibleModel, observationSequence.length));
        ObservationSequenceLogLikelihoods forwardLikelihoods =
                new SequenceScorer().scoreForSequence(
                        observationSequence, createForwardScorers(possibleModel, observationSequence.length));
        return mergeLikelihoods(backwardLikelihoods, forwardLikelihoods);
    }

    private ObservationSequenceLogLikelihoods mergeLikelihoods(
        ObservationSequenceLogLikelihoods backwardLikelihoods,
        ObservationSequenceLogLikelihoods forwardLikelihoods)
    {
        ArrayList<NodeLogLikelihoods> merged = new ArrayList<NodeLogLikelihoods>();
        
        // TODO Auto-generated method stub
        return new ObservationSequenceLogLikelihoods(forwardLikelihoods.getLogLikelihood(), merged);
    }

    private NodeScorer[][] createForwardScorers(Node possibleModel, int length)
    {
        Node[] allNodes = getAllNodes(possibleModel);
        NodeScorer[][] scorers = new NodeScorer[length][allNodes.length];
        for (int i = 0; i < length; ++i) {
            for (int j = 0; j < allNodes.length; ++j) {
                scorers[i][j] = new NodeScorer(allNodes[j], null);
            }
        }
        
        // TODO Auto-generated method stub
        return null;
    }

    private NodeScorer[][] createBackwardScorers(Node possibleModel, int length)
    {
        Node[] allNodes = getAllNodes(possibleModel);
        NodeScorer[][] scorers = new NodeScorer[length][allNodes.length];
        // TODO Auto-generated method stub
        return null;
    }

    private Node[] getAllNodes(Node possibleModel)
    {
        // TODO Auto-generated method stub
        return null;
    }
}

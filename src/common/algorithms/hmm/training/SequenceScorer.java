package common.algorithms.hmm.training;

import java.util.ArrayList;

public class SequenceScorer
{
    public ObservationSequenceLogLikelihoods scoreForSequence(
        double[][] sequence,
        NodeScorer[][] scorers)
    {
        for (int i = 0; i < sequence.length; ++i)
            for (int j = 0; j < scorers[i].length; ++j)
                scorers[i][j].scoreForObservation(sequence[i]);
        
        float bestScore = Float.NEGATIVE_INFINITY;
        for (int j = 0; j < scorers[scorers.length - 1].length; ++j) {
            float currentScore = scorers[scorers.length - 1][j].getScore();
            if (currentScore > bestScore) bestScore = currentScore;
        }
        
        ArrayList<NodeLogLikelihoods> nodesLogLikelihoods = new ArrayList<NodeLogLikelihoods>();
        for (int i = sequence.length - 1; i >= 0; --i) {
            for (int j = 0; j < scorers[i].length; ++j) {
                nodesLogLikelihoods.add(createNodeLogLikelihoods(scorers[i][j], sequence[i]));
            }
        }
        return new ObservationSequenceLogLikelihoods(bestScore, nodesLogLikelihoods);
    }

    private NodeLogLikelihoods createNodeLogLikelihoods(NodeScorer nodeScorer, double[] observation)
    {
        ArrayList<ArcLogLikelihood> arcLikelihoods = new ArrayList<ArcLogLikelihood>();
        
        for (NodeScorerArc arc : nodeScorer) {
            arcLikelihoods.add(new ArcLogLikelihood(arc.getArc(), arc.getScore()));
        }
        return new NodeLogLikelihoods(
                nodeScorer.getNode(), observation, nodeScorer.getScore(), arcLikelihoods);
    }
}

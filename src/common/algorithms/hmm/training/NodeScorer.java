package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.Iterator;

import common.algorithms.hmm.LogMath;
import common.algorithms.hmm.Node;
import common.exceptions.ImplementationError;

public class NodeScorer implements Iterable<NodeScorerArc>
{
    private float score = 0;
    private float scoreWithoutObservation = 0;
    private ArrayList<NodeScorerArc> previousNodesScorers;
    private Node node;
    
    public NodeScorer(Node node, ArrayList<NodeScorerArc> previousNodes, float initialScore)
    {
        this.node = node;
        this.previousNodesScorers = previousNodes;
        this.score = initialScore;
        this.scoreWithoutObservation = initialScore;
    }
    
    public void scoreForObservation(double[] observation) throws ImplementationError
    {
        if (this.node == null) {
            throw new ImplementationError("node in nodes scorers is null");
        }
        float currentScore = this.node.getState().observationLogLikelihood(observation);
        float arcSum = Float.NEGATIVE_INFINITY;
        for (NodeScorerArc arc : this.previousNodesScorers) {
            arcSum = LogMath.logAdd(arcSum, arc.getScore());
        }
        if (arcSum > 0)
            throw new ImplementationError("arc probability sum is greater than 0: " + arcSum);
        if (currentScore > 0)
            throw new ImplementationError("current observation probability is greater than 0: " + currentScore);
        this.score = arcSum + currentScore;
        this.scoreWithoutObservation = arcSum;
    }

    public float getScore()
    {
        return this.score;
    }
    public float getScoreWithoutObservation()
    {
        return this.scoreWithoutObservation;
    }
    
    public Node getNode()
    {
        return this.node;
    }

    @Override
    public Iterator<NodeScorerArc> iterator()
    {
        return this.previousNodesScorers.iterator();
    }
}

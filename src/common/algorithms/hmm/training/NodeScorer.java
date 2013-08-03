package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.Iterator;

import common.algorithms.hmm.LogMath;
import common.algorithms.hmm.Node;

public class NodeScorer implements Iterable<NodeScorerArc>
{
    private float score = Float.POSITIVE_INFINITY;
    private ArrayList<NodeScorerArc> previousNodesScorers;
    private Node node;
    
    public NodeScorer(Node node, ArrayList<NodeScorerArc> previousNodes)
    {
        this.node = node;
        this.previousNodesScorers = previousNodes;
    }
    
    public void scoreForObservation(double[] observation)
    {
        float currentScore =
            (this.node == null) ? 0 : this.node.getState().observationLogLikelihood(observation);
        float arcSum = 0;
        for (NodeScorerArc arc : this.previousNodesScorers) {
            arcSum = LogMath.logAdd(arcSum, arc.getScore());
        }
        this.score = arcSum + currentScore;
    }

    public float getScore()
    {
        return this.score;
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

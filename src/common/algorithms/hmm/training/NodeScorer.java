package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.Iterator;

import common.LogMath;
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
        LogMath arcSum = new LogMath();
        for (NodeScorerArc arc : this.previousNodesScorers) {
            arcSum.logAdd(arc.getScore());
        }
        if (arcSum.getResult() > 0)
            throw new ImplementationError("arc probability sum is greater than 0: " + arcSum);
        if (currentScore > 0)
            throw new ImplementationError("current observation probability is greater than 0: " + currentScore);
        this.score = arcSum.getResult() + currentScore;
        this.scoreWithoutObservation = arcSum.getResult();
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

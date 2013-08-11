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
    private final ArrayList<NodeScorerArc> previousNodesScorers;
    private final Node node;
    private final boolean isExitting;
    
    public NodeScorer(
        Node node,
        ArrayList<NodeScorerArc> previousNodes,
        float initialScore,
        boolean exitting)
    {
        this.node = node;
        this.previousNodesScorers = previousNodes;
        this.score = initialScore;
        this.scoreWithoutObservation = initialScore;
        this.isExitting = exitting;
    }
    
    public void scoreForObservation(double[] observation) throws ImplementationError
    {
//        System.err.println(this.node);
        if (this.node == null) {
            throw new ImplementationError("node in nodes scorers is null");
        }
        float observationScore = this.node.getState().observationLogLikelihood(observation);
        LogMath arcSum = new LogMath();
        for (NodeScorerArc arc : this.previousNodesScorers) {
            arcSum.logAdd(arc.getScore());
        }
//        if (arcSum.getResult() > 0)
//            throw new ImplementationError("arc probability sum is greater than 0: " + arcSum.getResult());
//        if (observationScore > 0)
//            throw new ImplementationError("current observation probability is greater than 0: " + observationScore);
        this.scoreWithoutObservation = arcSum.getResult(-Float.MAX_VALUE);
//        System.err.println(this.node + " " + this.scoreWithoutObservation + " " + this.score);
        this.score = this.scoreWithoutObservation + observationScore;
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
    
    public boolean isExitting()
    {
        return this.isExitting;
    }

    @Override
    public Iterator<NodeScorerArc> iterator()
    {
        return this.previousNodesScorers.iterator();
    }
}

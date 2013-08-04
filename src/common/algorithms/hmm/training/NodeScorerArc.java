package common.algorithms.hmm.training;

import common.algorithms.hmm.Arc;

public class NodeScorerArc
{
    private final NodeScorer from;
    private final Arc arc;
    
    public NodeScorerArc(NodeScorer from, Arc arc)
    {
        this.from = from;
        this.arc = arc;
    }
    public float getScore()
    {
        return this.arc.getExit().getLogLikelihood() +
                ((this.from == null) ? 0 : this.from.getScore());
    }
    public Arc getArc()
    {
        return this.arc;
    }
}

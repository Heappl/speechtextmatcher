package common.algorithms.hmm.training;

import common.algorithms.hmm.Arc;
import common.exceptions.ImplementationError;

public class NodeScorerArc
{
    private final NodeScorer from;
    private final Arc arc;
    
    public NodeScorerArc(NodeScorer from, Arc arc)
    {
        this.from = from;
        this.arc = arc;
    }
    public float getScore() throws ImplementationError
    {
        float arcScore = this.arc.getExit().getLogLikelihood();
//        if (arcScore > 0)
//            throw new ImplementationError("arc score is greater than 0: " + arcScore);
        float fromScore = ((this.from == null) ? 0 : this.from.getScore());
//        if (fromScore > 0)
//            throw new ImplementationError("from node score is greater than 0: " + fromScore);
//        if (arcScore + fromScore > 0)
//            throw new ImplementationError("arc total score is greater than 0: " + (arcScore + fromScore));
        return arcScore + fromScore;
    }
    public Arc getArc()
    {
        return this.arc;
    }
}

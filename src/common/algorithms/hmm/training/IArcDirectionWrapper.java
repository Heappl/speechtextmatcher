package common.algorithms.hmm.training;

import common.algorithms.hmm.Arc;
import common.algorithms.hmm.Node;
import common.algorithms.hmm.StateExit;

public interface IArcDirectionWrapper
{
    Node getEntryNode(Arc arc);
    Node getExitNode(Arc arc);
    Arc createArc(StateExit exit, Node from, Node to);
    
    public final static IArcDirectionWrapper forwardArcWrapper = new IArcDirectionWrapper() {
        @Override
        public Node getEntryNode(Arc arc)
        {
            return arc.getOutgoingFromNode();
        }
        @Override
        public Node getExitNode(Arc arc)
        {
            return arc.getLeadingToNode();
        }
        @Override
        public Arc createArc(StateExit exit, Node from, Node to)
        {
            return new Arc(exit, to, from);
        }
    };
    public final static IArcDirectionWrapper backwardArcWrapper = new IArcDirectionWrapper() {
        @Override
        public Node getEntryNode(Arc arc)
        {
            return arc.getLeadingToNode();
        }
        @Override
        public Node getExitNode(Arc arc)
        {
            return arc.getOutgoingFromNode();
        }
        @Override
        public Arc createArc(StateExit exit, Node from, Node to)
        {
            return new Arc(exit, from, to);
        }
    };
}

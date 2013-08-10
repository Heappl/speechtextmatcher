package common.algorithms.hmm.training;

import common.algorithms.hmm.Arc;
import common.algorithms.hmm.Node;

public interface IArcDirectionWrapper
{
    Node getEntryNode(Arc arc);
    Node getExitNode(Arc arc);
    
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
    };
}

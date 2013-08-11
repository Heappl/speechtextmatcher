package common.algorithms.hmm.training;

import common.algorithms.hmm.Arc;
import common.algorithms.hmm.Node;
import common.algorithms.hmm.StateExit;

public interface IArcDirectionWrapper
{
    Node getEntryNode(Arc arc);
    Node getExitNode(Arc arc);
    Arc createArc(StateExit exit, Node from, Node to);
    boolean isStartingNode(Node node, Node entryNode);
    boolean isExittingNode(Node node, Node entryNode);
    
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
        @Override
        public boolean isStartingNode(Node node, Node entryNode)
        {
            return (node == entryNode);
        }
        @Override
        public boolean isExittingNode(Node node, Node entryNode)
        {
            for (Arc arc : node) {
                if (arc.getLeadingToNode() == null)
                    return true;
            }
            return false;
        }
    };
    public final static IArcDirectionWrapper backwardArcWrapper = new IArcDirectionWrapper() {
        @Override
        public Node getEntryNode(Arc arc)
        {
            return forwardArcWrapper.getExitNode(arc);
        }
        @Override
        public Node getExitNode(Arc arc)
        {
            return forwardArcWrapper.getEntryNode(arc);
        }
        @Override
        public Arc createArc(StateExit exit, Node from, Node to)
        {
            return forwardArcWrapper.createArc(exit, to, from);
        }
        @Override
        public boolean isStartingNode(Node node, Node entryNode)
        {
            return forwardArcWrapper.isExittingNode(node, entryNode);
        }
        @Override
        public boolean isExittingNode(Node node, Node entryNode)
        {
            return forwardArcWrapper.isStartingNode(node, entryNode);
        }
    };
}

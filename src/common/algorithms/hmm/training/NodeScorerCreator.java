package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.algorithms.hmm.Arc;
import common.algorithms.hmm.Node;
import common.algorithms.hmm.StateExit;
import common.exceptions.ImplementationError;

public class NodeScorerCreator
{
    
    private int sequenceLength;
    
    public NodeScorerCreator(int sequenceLength)
    {
        this.sequenceLength = sequenceLength;
    }

    public NodeScorer[][] createForwardScorers(Node possibleModel) throws ImplementationError
    {
        return createScorers(possibleModel, sequenceLength, IArcDirectionWrapper.forwardArcWrapper);
    }
    public NodeScorer[][] createBackwardScorers(Node possibleModel) throws ImplementationError
    {
        return createScorers(possibleModel, sequenceLength, IArcDirectionWrapper.backwardArcWrapper);
    }

    private NodeScorer[][] createScorers(
        Node possibleModel, int length, final IArcDirectionWrapper arcWrapper) throws ImplementationError
    {
        final Node[] allNodes = getAllNodes(possibleModel);
        boolean[] isExitting = new boolean[allNodes.length];
        for (int i = 0; i < allNodes.length; ++i)
            isExitting[i] = arcWrapper.isExittingNode(allNodes[i], possibleModel);
        
        int startNodeIndex = findStartingNode(possibleModel, allNodes, arcWrapper);
        if (startNodeIndex < 0)
            throw new ImplementationError("entry node not in all nodes list");
        final Map<Node, Integer> indexes = new HashMap<Node, Integer>();
        for (int i = 0; i < allNodes.length; ++i) indexes.put(allNodes[i], i);
        
        final Map<Node, ArrayList<Arc>> incomingArcs = new HashMap<Node, ArrayList<Arc>>();
        for (Node node : allNodes) {
            for (Arc arc : node) {
                Node nextNode = arcWrapper.getExitNode(arc);
                if (!incomingArcs.containsKey(nextNode))
                    incomingArcs.put(nextNode, new ArrayList<Arc>());
                incomingArcs.get(nextNode).add(arc);
            }
        }
        
        NodeScorer[][] scorers = new NodeScorer[length][allNodes.length];
        NodeScorer initial = new NodeScorer(new Node("", null), new ArrayList<NodeScorerArc>(), 0, false);

        for (int i = 0; i < length; ++i) {
            for (int j = 0; j < scorers[i].length; ++j) {
                ArrayList<NodeScorerArc> arcScorers = new ArrayList<NodeScorerArc>();
                if (i > 0) {
                    for (Arc arc : incomingArcs.get(allNodes[j])) {
                        Node entryNode = arcWrapper.getEntryNode(arc);
                        if (entryNode == null) continue;
                        arcScorers.add(
                            new NodeScorerArc(scorers[i - 1][indexes.get(entryNode)], arc));
                    }
                } else if (j == startNodeIndex) {
                    StateExit intialTransition = new StateExit();
                    intialTransition.updateLikelihood(0);
                    arcScorers.add(
                        new NodeScorerArc(
                            initial,
                            arcWrapper.createArc(intialTransition, initial.getNode(), possibleModel)));
                }
                scorers[i][j] = new NodeScorer(allNodes[j], arcScorers, Float.NaN, isExitting[j]);
            }
        }
        return scorers;
    }

    private int findStartingNode(Node starting, Node[] allNodes, IArcDirectionWrapper arcWrapper)
    {
        for (int i = 0; i < allNodes.length; ++i)
            if (arcWrapper.isStartingNode(allNodes[i], starting))
                return i;
        return -1;
    }

    private Node[] getAllNodes(Node starting)
    {
        Set<Node> ret = new HashSet<Node>();
        ArrayList<Node> next = new ArrayList<Node>();
        next.add(starting);
        ret.add(starting);
        while (!next.isEmpty()) {
            Node current = next.remove(next.size() - 1);
            for (Arc arc : current) {
                if (ret.contains(arc.getLeadingToNode())) continue;
                if (arc.isExitState()) continue;
                next.add(arc.getLeadingToNode());
                ret.add(arc.getLeadingToNode());
            }
        }
        return ret.toArray(new Node[0]);
    }
}

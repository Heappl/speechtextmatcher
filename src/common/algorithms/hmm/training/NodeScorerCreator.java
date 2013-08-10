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
        Node possibleModel, int length, final IArcDirectionWrapper arcCreator) throws ImplementationError
    {
        final Node[] allNodes = getAllNodes(possibleModel);
        int startNodeIndex = findStartingNode(possibleModel, allNodes);
        if (startNodeIndex < 0)
            throw new ImplementationError("entry node not in all nodes list");
        final Map<Node, Integer> indexes = new HashMap<Node, Integer>();
        for (int i = 0; i < allNodes.length; ++i) indexes.put(allNodes[i], i);
        
        NodeScorer[][] scorers = new NodeScorer[length][allNodes.length];
        NodeScorer initial = new NodeScorer(new Node("", null), new ArrayList<NodeScorerArc>(), 0);

        for (int i = 0; i < length; ++i) {
            for (int j = 0; j < scorers[i].length; ++j) {
                ArrayList<NodeScorerArc> arcScorers = new ArrayList<NodeScorerArc>();
                if (i > 0) {
                    for (Arc arc : allNodes[j]) {
                        Node entryNode = arcCreator.getEntryNode(arc);
                        if (entryNode == null) continue;
                        arcScorers.add(
                            new NodeScorerArc(scorers[i - 1][indexes.get(entryNode)], arc));
                    }
                } else if (j == startNodeIndex) {
                    StateExit intialTransition = new StateExit();
                    intialTransition.updateLikelihood(0);
                    arcScorers.add(new NodeScorerArc(initial, new Arc(intialTransition, null, allNodes[0])));
                }
                scorers[i][j] = new NodeScorer(allNodes[j], arcScorers, Float.NaN);
            }
        }
        return scorers;
    }

    private int findStartingNode(Node starting, Node[] allNodes)
    {
        for (int i = 0; i < allNodes.length; ++i)
            if (starting == allNodes[i])
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

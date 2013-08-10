package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;

import common.LogMath;
import common.algorithms.hmm.Arc;
import common.algorithms.hmm.Node;
import common.algorithms.hmm.StateExit;
import common.exceptions.ImplementationError;

public class NodeLogLikelihoodsCalculator
{
    private interface ScorerArcCreator
    {
        Node getEntryNode(Arc arc);
    }
    private final static ScorerArcCreator forwardArcCreator = new ScorerArcCreator() {
        @Override
        public Node getEntryNode(Arc arc)
        {
            return arc.getOutgoingFromNode();
        }
    };
    private final static ScorerArcCreator backwardArcCreator = new ScorerArcCreator() {
        @Override
        public Node getEntryNode(Arc arc)
        {
            return arc.getLeadingToNode();
        }
    };
    
    public ObservationSequenceLogLikelihoods calculate(
        double[][] observationSequence,
        Node possibleModel) throws ImplementationError
    {
        ArrayList<double[]> sequence = new ArrayList<double[]>();
        for (double[] observation : observationSequence) sequence.add(observation);
        
        ObservationSequenceLogLikelihoods forwardLikelihoods =
                new SequenceScorer().scoreForSequence(
                        sequence, createScorers(possibleModel, sequence.size(), forwardArcCreator));
        checkLikelihoods(forwardLikelihoods);
        
        Collections.reverse(sequence);
        ObservationSequenceLogLikelihoods backwardLikelihoods =
                new SequenceScorer().scoreForSequence(
                        sequence, createScorers(possibleModel, sequence.size(), backwardArcCreator));
        checkLikelihoods(backwardLikelihoods);
        
        ObservationSequenceLogLikelihoods merged = mergeLikelihoods(backwardLikelihoods, forwardLikelihoods);
        checkLikelihoods(merged);
        ObservationSequenceLogLikelihoods normalized = normalize(merged);
        checkLikelihoods(normalized);
        return normalized;
    }

    private void checkLikelihoods(ObservationSequenceLogLikelihoods merged) throws ImplementationError
    {
        if (merged.getLogLikelihood() == Float.NEGATIVE_INFINITY)
            throw new ImplementationError("sequence likelihood is negative infinity");
        if (merged.getLogLikelihood() > 0)
            throw new ImplementationError("sequence likelihood is greater than 0: " + merged.getLogLikelihood());
        
        for (NodeLogLikelihoods nodeLL : merged) {
            checkNodeLikelihoods(nodeLL);
        }
    }

    private void checkNodeLikelihoods(NodeLogLikelihoods nodeLL) throws ImplementationError
    {
        if (nodeLL.getLogLikelihood() > 0)
            throw new ImplementationError("node likelihood is greater than 0: " + nodeLL.getLogLikelihood());
        if (nodeLL.getLogLikelihoodWithoutObservation() > 0)
            throw new ImplementationError("node likelihood wo observation is greater than 0: "
                    + nodeLL.getLogLikelihoodWithoutObservation());
        if (nodeLL.getLogLikelihood() > nodeLL.getLogLikelihoodWithoutObservation())
            throw new ImplementationError("node likelihood is greater than node likelihood without observation: "
                    + "(" + nodeLL.getLogLikelihood() + " > " + nodeLL.getLogLikelihoodWithoutObservation() + ")");
   
        LogMath arcSum = new LogMath();
        for (ArcLogLikelihood arcLL : nodeLL) {
            arcSum.logAdd(checkArcLikelihoods(arcLL));
        }
        if (arcSum.resultIsSet() && (arcSum.getResult() != nodeLL.getLogLikelihoodWithoutObservation()))
            throw new ImplementationError(
                    nodeLL.getNode().getName() + " " +
                    "sum of probabilities of incoming arcs is different" +
                    " than probability of being at node at given time (" +
                    arcSum.getResult() + " != " + nodeLL.getLogLikelihood() + ")");
    }

    private float checkArcLikelihoods(ArcLogLikelihood arcLL) throws ImplementationError
    {
        if (arcLL.getLogLikelihood() > 0)
            throw new ImplementationError("arc likelihood is greater than 0: " + arcLL.getLogLikelihood());
        return arcLL.getLogLikelihood();
    }

    private NodeScorer[][] createScorers(
        Node possibleModel, int length, final ScorerArcCreator arcCreator) throws ImplementationError
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

    private ObservationSequenceLogLikelihoods mergeLikelihoods(
        ObservationSequenceLogLikelihoods backwardLikelihoods,
        ObservationSequenceLogLikelihoods forwardLikelihoods) throws ImplementationError
    {
        Map<double[], Map<Node, NodeLogLikelihoods>> forwardNodeLLs =
                createMapOfNodeLikelihoods(forwardLikelihoods);
        Map<double[], Map<Node, NodeLogLikelihoods>> backwardNodeLLs =
                createMapOfNodeLikelihoods(forwardLikelihoods);
        
        ArrayList<NodeLogLikelihoods> merged = new ArrayList<NodeLogLikelihoods>();
        
        for (NodeLogLikelihoods likelihood : forwardLikelihoods) {
            merged.add(createMergedLikelihood(likelihood, forwardNodeLLs, backwardNodeLLs));
        }
        return new ObservationSequenceLogLikelihoods(forwardLikelihoods.getLogLikelihood(), merged);
    }

    private Map<double[], Map<Node, NodeLogLikelihoods>> createMapOfNodeLikelihoods(
        ObservationSequenceLogLikelihoods likelihoods)
    {
        Map<double[], Map<Node, NodeLogLikelihoods>> ret = new HashMap<double[], Map<Node,NodeLogLikelihoods>>();
        for (NodeLogLikelihoods likelihood : likelihoods) {
            if (!ret.containsKey(likelihood.getObservation()))
                ret.put(likelihood.getObservation(), new HashMap<Node, NodeLogLikelihoods>());
            ret.get(likelihood.getObservation()).put(likelihood.getNode(), likelihood);
        }
        return ret;
    }

    private NodeLogLikelihoods createMergedLikelihood(
        NodeLogLikelihoods arcsLikelihood,
        Map<double[], Map<Node, NodeLogLikelihoods>> forwardNodeLLs,
        Map<double[], Map<Node, NodeLogLikelihoods>> backwardNodeLLs) throws ImplementationError
    {
        double[] observation = arcsLikelihood.getObservation();
        Node node = arcsLikelihood.getNode();
        
        NodeLogLikelihoods endingLikelihood = backwardNodeLLs.get(observation).get(node);
        double[] previousObservation = endingLikelihood.getNextObservation();

        ArrayList<ArcLogLikelihood> arcLikelihoods = new ArrayList<ArcLogLikelihood>();
        for (ArcLogLikelihood arc : arcsLikelihood) {
            Node previousNode = arc.getArc().getOutgoingFromNode();
            if (previousNode == null) continue;
            NodeLogLikelihoods startingLikelihood = null;
            if (previousObservation != null)
                startingLikelihood = forwardNodeLLs.get(previousObservation).get(previousNode);
            arcLikelihoods.add(createArcLikelihoods(startingLikelihood, endingLikelihood, arc.getArc()));
        }

        NodeLogLikelihoods startingCurrentLikelihood = forwardNodeLLs.get(observation).get(node);
        float mergedNodeLikelihood =
                endingLikelihood.getLogLikelihood() + startingCurrentLikelihood.getLogLikelihoodWithoutObservation();
        float otherMergedLikelihood =
                endingLikelihood.getLogLikelihoodWithoutObservation() + startingCurrentLikelihood.getLogLikelihood();
        if (mergedNodeLikelihood != otherMergedLikelihood) {
            throw new ImplementationError("merged node likelihoods don't agree: "
                    + mergedNodeLikelihood + " != " + otherMergedLikelihood + " "
                    + "(ending: " + endingLikelihood.getLogLikelihood() + " "
                    + endingLikelihood.getLogLikelihoodWithoutObservation() + ", "
                    + "starting: " + startingCurrentLikelihood.getLogLikelihood() + " "
                    + startingCurrentLikelihood.getLogLikelihoodWithoutObservation() + ")");
        }
        if (mergedNodeLikelihood > 0)
            throw new ImplementationError("node likelihood is greater than 0: " + mergedNodeLikelihood);
        float mergedLikelihoodWithoutObservation =
                endingLikelihood.getLogLikelihoodWithoutObservation() +
                startingCurrentLikelihood.getLogLikelihoodWithoutObservation();
        if (mergedLikelihoodWithoutObservation < mergedNodeLikelihood) {
            throw new ImplementationError(
                "merged node likelihood without observation is smaller than the one with observation: " +
                        mergedLikelihoodWithoutObservation + " < " + mergedNodeLikelihood);
        }
        return new NodeLogLikelihoods(
                node,
                observation,
                arcsLikelihood.getNextObservation(),
                mergedNodeLikelihood,
                mergedLikelihoodWithoutObservation,
                arcLikelihoods);
    }

    private ArcLogLikelihood createArcLikelihoods(
        NodeLogLikelihoods startArcLikelihood,
        NodeLogLikelihoods endingArcLikelihood,
        Arc arc) throws ImplementationError
    {
        float likelihood =
                ((startArcLikelihood == null) ? 0 : startArcLikelihood.getLogLikelihood())
                + arc.getExit().getLogLikelihood()
                + ((endingArcLikelihood == null) ? 0 : endingArcLikelihood.getLogLikelihood());
        if (likelihood > 0)
            throw new ImplementationError("arc likelihood is greater than 0: " + likelihood);
        return new ArcLogLikelihood(arc, likelihood);
    }

    private ObservationSequenceLogLikelihoods normalize(
        ObservationSequenceLogLikelihoods likelihoods)
    {
        Map<double[], ArrayList<NodeLogLikelihoods>> likelihoodsPerObservation =
                new HashMap<double[], ArrayList<NodeLogLikelihoods>>();
        for (NodeLogLikelihoods nodeLikelihoods : likelihoods) {
            ArrayList<NodeLogLikelihoods> observationLikelihoods = new ArrayList<NodeLogLikelihoods>();
            if (likelihoodsPerObservation.containsKey(nodeLikelihoods.getObservation()))
                observationLikelihoods = likelihoodsPerObservation.get(nodeLikelihoods.getObservation());
            
            observationLikelihoods.add(nodeLikelihoods);
            likelihoodsPerObservation.put(nodeLikelihoods.getObservation(), observationLikelihoods);
        }
        
        ArrayList<NodeLogLikelihoods> normalized = new ArrayList<NodeLogLikelihoods>();
        for (double[] key : likelihoodsPerObservation.keySet()) {
            ArrayList<NodeLogLikelihoods> observationLikelihoods = likelihoodsPerObservation.get(key);
            LogMath totalObservationLogLikelihood = new LogMath();
            for (NodeLogLikelihoods nodeLikelihoods : observationLikelihoods) {
                totalObservationLogLikelihood.logAdd(nodeLikelihoods.getLogLikelihood());
            }
            for (NodeLogLikelihoods nodeLikelihoods : observationLikelihoods) {
                normalized.add(new NodeLogLikelihoods(
                    nodeLikelihoods,
                    nodeLikelihoods.getLogLikelihood() - totalObservationLogLikelihood.getResult()));
            }
        }
        return new ObservationSequenceLogLikelihoods(
                likelihoods.getLogLikelihood(), normalized);
    }
}

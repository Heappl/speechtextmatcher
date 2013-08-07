package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;

import common.algorithms.hmm.Arc;
import common.algorithms.hmm.LogMath;
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
        ObservationSequenceLogLikelihoods backwardLikelihoods =
                new SequenceScorer().scoreForSequence(
                        sequence, createScorers(possibleModel, sequence.size(), backwardArcCreator));
        Collections.reverse(sequence);
        ObservationSequenceLogLikelihoods forwardLikelihoods =
                new SequenceScorer().scoreForSequence(
                        sequence, createScorers(possibleModel, sequence.size(), forwardArcCreator));
        
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
        
        float sum = Float.NEGATIVE_INFINITY;
        for (ArcLogLikelihood arcLL : nodeLL) {
            sum = LogMath.logAdd(sum, checkArcLikelihoods(arcLL));
        }
        if (sum < nodeLL.getLogLikelihood())
            throw new ImplementationError(
                    "sum of probabilities of incoming arcs is less" +
                            " than probability of reaching node with observing node in it (" +
                            sum + " < " + nodeLL.getLogLikelihood() + ")");
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
                scorers[i][j] = new NodeScorer(allNodes[j], arcScorers, Float.NEGATIVE_INFINITY);
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
        ObservationSequenceLogLikelihoods forwardLikelihoods)
    {
        Map<double[], Map<Node, NodeLogLikelihoods>> backwardNodeLLs =
                new HashMap<double[], Map<Node,NodeLogLikelihoods>>();
        for (NodeLogLikelihoods likelihood : backwardLikelihoods) {
            if (!backwardNodeLLs.containsKey(likelihood.getObservation()))
                backwardNodeLLs.put(likelihood.getObservation(), new HashMap<Node, NodeLogLikelihoods>());
            backwardNodeLLs.get(likelihood.getObservation()).put(likelihood.getNode(), likelihood);
        }
        
        ArrayList<NodeLogLikelihoods> merged = new ArrayList<NodeLogLikelihoods>();
        
        for (NodeLogLikelihoods likelihood : forwardLikelihoods) {
            merged.add(createMergedLikelihood(likelihood, backwardNodeLLs));
        }
        return new ObservationSequenceLogLikelihoods(forwardLikelihoods.getLogLikelihood(), merged);
    }

    private NodeLogLikelihoods createMergedLikelihood(
        NodeLogLikelihoods likelihood,
        Map<double[], Map<Node, NodeLogLikelihoods>> backwardNodeLLs)
    {
        double[] observation = likelihood.getObservation();
        Node node = likelihood.getNode();
        NodeLogLikelihoods endingLikelihood = backwardNodeLLs.get(observation).get(node);
        
        ArrayList<ArcLogLikelihood> arcLikelihoods = new ArrayList<ArcLogLikelihood>();
        for (ArcLogLikelihood arc : likelihood) {
            double[] nextObs = arc.getNextObservation();
            NodeLogLikelihoods endingArcLikelihood =
                    (nextObs == null) ? null :
                        backwardNodeLLs.get(nextObs).get(arc.getArc().getLeadingToNode());
            arcLikelihoods.add(createArcLikelihoods(likelihood , endingArcLikelihood, arc.getArc()));
        }
        return new NodeLogLikelihoods(
                node,
                observation,
                likelihood.getLogLikelihood() + endingLikelihood.getLogLikelihoodWithoutObservation(),
                likelihood.getLogLikelihoodWithoutObservation() + endingLikelihood.getLogLikelihoodWithoutObservation(),
                arcLikelihoods);
    }

    private ArcLogLikelihood createArcLikelihoods(
        NodeLogLikelihoods startArcLikelihood,
        NodeLogLikelihoods endingArcLikelihood,
        Arc arc)
    {
        float likelihood = startArcLikelihood.getLogLikelihood()
                + arc.getExit().getLogLikelihood();
        if (endingArcLikelihood != null)
            likelihood += endingArcLikelihood.getLogLikelihood();
        return new ArcLogLikelihood(arc, likelihood,
                (endingArcLikelihood == null) ? null : endingArcLikelihood.getObservation());
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
            float totalObservationLogLikelihood = Float.NEGATIVE_INFINITY;
            for (NodeLogLikelihoods nodeLikelihoods : observationLikelihoods) {
                totalObservationLogLikelihood =
                    LogMath.logAdd(totalObservationLogLikelihood, nodeLikelihoods.getLogLikelihood());
            }
            for (NodeLogLikelihoods nodeLikelihoods : observationLikelihoods) {
                normalized.add(new NodeLogLikelihoods(
                        nodeLikelihoods,
                        nodeLikelihoods.getLogLikelihood() - totalObservationLogLikelihood));
            }
        }
        return new ObservationSequenceLogLikelihoods(
                likelihoods.getLogLikelihood(), normalized);
    }
}

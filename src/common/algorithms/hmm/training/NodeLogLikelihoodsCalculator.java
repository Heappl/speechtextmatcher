package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.algorithms.hmm.Arc;
import common.algorithms.hmm.LogMath;
import common.algorithms.hmm.Node;

public class NodeLogLikelihoodsCalculator
{
    private interface ScorerArcCreator
    {
        void iterate(NodeScorer[][] scorers, ScorerCreator creator);
        Node getExitNode(Arc arc);
    }
    private interface ScorerCreator
    {
        void create(NodeScorer[] current, NodeScorer[] previous);
    }
    private final static ScorerArcCreator forwardArcCreator = new ScorerArcCreator() {
        @Override
        public void iterate(NodeScorer[][] scorers, ScorerCreator creator)
        {
            creator.create(scorers[0], new NodeScorer[scorers[0].length]);
            for (int i = 1; i < scorers.length; ++i)
                 creator.create(scorers[i], scorers[i - 1]);
        }
        @Override
        public Node getExitNode(Arc arc)
        {
            return arc.getLeadingToNode();
        }
    };
    private final static ScorerArcCreator backwardArcCreator = new ScorerArcCreator() {
        @Override
        public void iterate(NodeScorer[][] scorers, ScorerCreator creator)
        {
            creator.create(scorers[scorers.length - 1], new NodeScorer[scorers[0].length]);
            for (int i = scorers.length - 2; i >= 0; --i)
                 creator.create(scorers[i], scorers[i + 1]);
        }
        @Override
        public Node getExitNode(Arc arc)
        {
            return arc.getOutgoingFromNode();
        }
    };
    
    public ObservationSequenceLogLikelihoods calculate(
        double[][] observationSequence,
        Node possibleModel)
    {
        ArrayList<double[]> sequence = new ArrayList<double[]>();
        for (double[] observation : observationSequence) sequence.add(observation);
        ObservationSequenceLogLikelihoods backwardLikelihoods =
                new SequenceScorer().scoreForSequence(
                        sequence, createScorers(possibleModel, sequence.size(), forwardArcCreator));
        Collections.reverse(sequence);
        ObservationSequenceLogLikelihoods forwardLikelihoods =
                new SequenceScorer().scoreForSequence(
                        sequence, createScorers(possibleModel, sequence.size(), backwardArcCreator));
        return normalize(mergeLikelihoods(backwardLikelihoods, forwardLikelihoods));
    }

    private NodeScorer[][] createScorers(
        Node possibleModel, int length, final ScorerArcCreator arcCreator)
    {
        final Node[] allNodes = getAllNodes(possibleModel);
        final Map<Node, Integer> indexes = new HashMap<Node, Integer>();
        for (int i = 0; i < allNodes.length; ++i) indexes.put(allNodes[i], i);
        
        NodeScorer[][] scorers = new NodeScorer[length][allNodes.length];
        
        arcCreator.iterate(scorers, new ScorerCreator() {
            @Override
            public void create(NodeScorer[] currentScorers, NodeScorer[] previousScorers)
            {
                for (int j = 0; j < currentScorers.length; ++j) {
                    
                    ArrayList<NodeScorerArc> arcScorers = new ArrayList<NodeScorerArc>();
                    for (Arc arc : allNodes[j]) {
                        Node exitNode = arcCreator.getExitNode(arc);
                        if (exitNode == null) continue;
                        arcScorers.add(
                            new NodeScorerArc(previousScorers[indexes.get(exitNode)], arc));
                    }
                    currentScorers[j] = new NodeScorer(allNodes[j], arcScorers);
                }
            }
        });
        return scorers;
    }

    private Node[] getAllNodes(Node starting)
    {
        Set<Node> ret = new HashSet<Node>();
        ArrayList<Node> next = new ArrayList<Node>();
        next.add(starting);
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
                likelihood.getLogLikelihood() + endingLikelihood.getLogLikelihood(),
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
            float totalObservationLogLikelihood = 0;
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

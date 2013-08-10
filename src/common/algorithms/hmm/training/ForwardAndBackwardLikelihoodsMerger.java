package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.algorithms.hmm.Arc;
import common.algorithms.hmm.Node;
import common.exceptions.ImplementationError;

public class ForwardAndBackwardLikelihoodsMerger
{
    public ObservationSequenceLogLikelihoods mergeLikelihoods(
        ObservationSequenceLogLikelihoods backwardLikelihoods,
        ObservationSequenceLogLikelihoods forwardLikelihoods) throws ImplementationError
    {
        Map<double[], Map<Node, NodeLogLikelihoods>> forwardNodeLLs =
                createMapOfNodeLikelihoods(forwardLikelihoods);
        Map<double[], Map<Node, NodeLogLikelihoods>> backwardNodeLLs =
                createMapOfNodeLikelihoods(backwardLikelihoods);
        
        ArrayList<NodeLogLikelihoods> merged = new ArrayList<NodeLogLikelihoods>();
        
        for (NodeLogLikelihoods likelihood : forwardLikelihoods) {
            merged.add(createMergedLikelihood(likelihood, forwardNodeLLs, backwardNodeLLs));
        }
        return new ObservationSequenceLogLikelihoods(forwardLikelihoods.getLogLikelihood(), merged);
    }

    private Map<double[], Map<Node, NodeLogLikelihoods>> createMapOfNodeLikelihoods(
        ObservationSequenceLogLikelihoods likelihoods)
    {
        Map<double[], Map<Node, NodeLogLikelihoods>> ret =
                new HashMap<double[], Map<Node,NodeLogLikelihoods>>();
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
            if (previousObservation == null) continue;
            Node previousNode = arc.getArc().getOutgoingFromNode();
            if (previousNode == null) continue;
            NodeLogLikelihoods startingLikelihood = forwardNodeLLs.get(previousObservation).get(previousNode);
            if (startingLikelihood.getNextObservation() != observation) {
                throw new ImplementationError(
                        "next observation forward<->backward don't agree "
                        + startingLikelihood.getNextObservation() + " " + observation);
            }
            arcLikelihoods.add(createArcLikelihoods(startingLikelihood, endingLikelihood, arc.getArc()));
        }

        NodeLogLikelihoods startingCurrentLikelihood = forwardNodeLLs.get(observation).get(node);
        float mergedNodeLikelihood =
                endingLikelihood.getLogLikelihood() + startingCurrentLikelihood.getLogLikelihoodWithoutObservation();
        float otherMergedLikelihood =
                endingLikelihood.getLogLikelihoodWithoutObservation() + startingCurrentLikelihood.getLogLikelihood();
        if (Math.abs(mergedNodeLikelihood - otherMergedLikelihood) > 0.1) {
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
}

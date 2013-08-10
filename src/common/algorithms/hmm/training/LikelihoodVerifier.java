package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.LogMath;
import common.exceptions.ImplementationError;

public class LikelihoodVerifier
{
    public void checkLikelihoods(
        ObservationSequenceLogLikelihoods merged,
        ArrayList<double[]> sequence,
        IArcDirectionWrapper arcWrapper,
        boolean checkIfSumsToOne) throws ImplementationError
    {
        if (merged.getLogLikelihood() == Float.NEGATIVE_INFINITY)
            throw new ImplementationError("sequence likelihood is negative infinity");
        if (merged.getLogLikelihood() > 0)
            throw new ImplementationError("sequence likelihood is greater than 0: " + merged.getLogLikelihood());
        
        Map<double[], ArrayList<NodeLogLikelihoods>> likelihoodsPerObservation =
                new HashMap<double[], ArrayList<NodeLogLikelihoods>>();
        for (NodeLogLikelihoods nodeLL : merged) {
            double[] next = findNextObservation(sequence, nodeLL.getObservation());
            checkNodeLikelihoods(nodeLL, next, arcWrapper);
            if (checkIfSumsToOne) {
                if (!likelihoodsPerObservation.containsKey(nodeLL.getObservation()))
                    likelihoodsPerObservation.put(
                            nodeLL.getObservation(), new ArrayList<NodeLogLikelihoods>());
                likelihoodsPerObservation.get(nodeLL.getObservation()).add(nodeLL);
            }
        }
        
        if (checkIfSumsToOne) {
            for (double[] observation : likelihoodsPerObservation.keySet()) {
                LogMath total = new LogMath();
                for (NodeLogLikelihoods nodeLL : likelihoodsPerObservation.get(observation))
                    total.logAdd(nodeLL.getLogLikelihood());
                if (Math.abs(total.getResult()) > 0.1)
                    throw new ImplementationError("sum of likelihoods per observation is not 0: " + total.getResult());
            }
        }
    }

    private double[] findNextObservation(ArrayList<double[]> sequence, double[] observation) throws ImplementationError
    {
        for (int i = 0; i < sequence.size(); ++i)
            if (sequence.get(i) == observation)
                return (i + 1 < sequence.size()) ? sequence.get(i + 1) : null;
        throw new ImplementationError("observation not found in a sequence");
    }

    private void checkNodeLikelihoods(
        NodeLogLikelihoods nodeLL,
        double[] nextObservation,
        IArcDirectionWrapper arcWrapper) throws ImplementationError
    {
        if (nodeLL.getLogLikelihood() > 0)
            throw new ImplementationError("node likelihood is greater than 0: " + nodeLL.getLogLikelihood());
        if (nodeLL.getLogLikelihoodWithoutObservation() > 0)
            throw new ImplementationError("node likelihood wo observation is greater than 0: "
                    + nodeLL.getLogLikelihoodWithoutObservation());
        if (nodeLL.getLogLikelihood() > nodeLL.getLogLikelihoodWithoutObservation())
            throw new ImplementationError("node likelihood is greater than node likelihood without observation: "
                    + "(" + nodeLL.getLogLikelihood() + " > " + nodeLL.getLogLikelihoodWithoutObservation() + ")");
        if (nodeLL.getNextObservation() != nextObservation) {
            throw new ImplementationError("next observation is not a next one in sequence");
        }
        
        LogMath arcSum = new LogMath();
        for (ArcLogLikelihood arcLL : nodeLL) {
            if (arcLL.getLogLikelihood() > 0)
                throw new ImplementationError("arc likelihood is greater than 0: " + arcLL.getLogLikelihood());
            if (arcWrapper.getExitNode(arcLL.getArc()) != nodeLL.getNode())
                throw new ImplementationError("arc is not incoming "
                        + arcWrapper.getExitNode(arcLL.getArc()) + " "
                        + nodeLL.getNode());
            arcSum.logAdd(arcLL.getLogLikelihood());
        }
        if (arcSum.resultIsSet()
                && (Math.abs(arcSum.getResult() - nodeLL.getLogLikelihoodWithoutObservation()) > 0.1))
            throw new ImplementationError(
                    nodeLL.getNode().getName() + " " +
                    "sum of probabilities of incoming arcs is different" +
                    " than node's probability (" +
                    arcSum.getResult() + " != " + nodeLL.getLogLikelihoodWithoutObservation() + ")");
    }
}

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
    
    public ObservationSequenceLogLikelihoods calculate(
        double[][] observationSequence,
        Node possibleModel) throws ImplementationError
    {
        ArrayList<double[]> sequence = new ArrayList<double[]>();
        for (double[] observation : observationSequence) sequence.add(observation);
        
        NodeScorerCreator nodeScorerCreator = new NodeScorerCreator(sequence.size());
        ObservationSequenceLogLikelihoods forwardLikelihoods = 
                new SequenceScorer().scoreForSequence(
                        sequence, nodeScorerCreator.createForwardScorers(possibleModel));
        new LikelihoodVerifier().checkLikelihoods(forwardLikelihoods, sequence, IArcDirectionWrapper.forwardArcWrapper);
        
        Collections.reverse(sequence);
        ObservationSequenceLogLikelihoods backwardLikelihoods = 
                new SequenceScorer().scoreForSequence(
                        sequence, nodeScorerCreator.createBackwardScorers(possibleModel));
        new LikelihoodVerifier().checkLikelihoods(backwardLikelihoods, sequence, IArcDirectionWrapper.backwardArcWrapper);

        Collections.reverse(sequence);
        ObservationSequenceLogLikelihoods merged =
                new ForwardAndBackwardLikelihoodsMerger().mergeLikelihoods(
                        backwardLikelihoods, forwardLikelihoods);
        new LikelihoodVerifier().checkLikelihoods(merged, sequence, IArcDirectionWrapper.forwardArcWrapper);
        ObservationSequenceLogLikelihoods normalized = normalize(merged);
        new LikelihoodVerifier().checkLikelihoods(normalized, sequence, IArcDirectionWrapper.forwardArcWrapper);
        return normalized;
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

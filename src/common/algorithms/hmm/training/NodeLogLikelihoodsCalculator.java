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
//        new LikelihoodVerifier().checkLikelihoods(
//                forwardLikelihoods, sequence, IArcDirectionWrapper.forwardArcWrapper);
        
        Collections.reverse(sequence);
        ObservationSequenceLogLikelihoods backwardLikelihoods = 
                new SequenceScorer().scoreForSequence(
                        sequence, nodeScorerCreator.createBackwardScorers(possibleModel));
//        new LikelihoodVerifier().checkLikelihoods(
//                backwardLikelihoods, sequence, IArcDirectionWrapper.backwardArcWrapper);

        Collections.reverse(sequence);
        ObservationSequenceLogLikelihoods merged =
                new ForwardAndBackwardLikelihoodsMerger().mergeLikelihoods(
                        backwardLikelihoods, forwardLikelihoods);
        return merged;
    }
}

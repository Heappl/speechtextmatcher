package common.algorithms.hmm;

import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.statistics.OnlineMultivariateDataStatistics;

public class MixtureModelWithHmmTraining
{
    HiddenMarkovModel trainModels(
        double[][][] setOfDataSequences,
        HMMGraph[] setOfStateGraphs)
    {
        HMMWithLogLikelihood hmm = createInitialHMM(setOfDataSequences, setOfStateGraphs);
        
        double previousLogLikelihood = hmm.logLikelihood;
        while (true) {
            hmm = trainNextHMM(setOfDataSequences, setOfStateGraphs, hmm.model);
            if (Math.abs(hmm.logLikelihood - previousLogLikelihood) < 0.001) break;
        }
        return hmm.model;
    }
    
    private class HMMWithLogLikelihood
    {
        double logLikelihood;
        HiddenMarkovModel model;
        
        public HMMWithLogLikelihood(HiddenMarkovModel model, double logLikelihood)
        {
            this.model = model;
            this.logLikelihood = logLikelihood;
        }
    }
    
    HMMWithLogLikelihood trainNextHMM(
        double[][][] setOfDataSequences,
        HMMGraph[] setOfStateGraphs,
        HiddenMarkovModel previousModel)
    {
        
        int states = 0; //TODO
        OnlineMultivariateDataStatistics[] statistics =
                new OnlineMultivariateDataStatistics[states];
        
        for (double[][] sequence : setOfDataSequences) {
            double[][] statesProbs =
                    previousModel.calculateStatesProbabilitesGivenSequenceOfObservations(sequence);
            for (int i = 0; i < sequence.length; ++i) {
                for (int j = 0; j < statistics.length; ++j) {
                    statistics[j].addPoint(sequence[i], statesProbs[i][j]);
                }
            }
        }
        
        MultivariateNormalDistribution[] stateDistributions = new MultivariateNormalDistribution[states];
        for (int i = 0; i < states; ++i)
            stateDistributions[i] = statistics[i].getDistribution();
        double[][] transitionProbabilities = null; //TODO
        
        return createNextHMM(setOfStateGraphs, stateDistributions, transitionProbabilities);
    }

    private HMMWithLogLikelihood createNextHMM(
        HMMGraph[] setOfStateGraphs,
        MultivariateNormalDistribution[] stateDistributions,
        double[][] transitionProbabilities)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private HMMWithLogLikelihood createInitialHMM(double[][][] setOfSequences, HMMGraph[] setOfStateGraphs)
    {
        // TODO Auto-generated method stub
        return null;
    }
}

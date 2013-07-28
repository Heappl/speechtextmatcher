package common.algorithms.hmm;

import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.statistics.OnlineMultivariateDataStatistics;

public class MixtureModelWithHmmTraining
{
    public HiddenMarkovModel trainModel(
        double[][][] setOfDataSequences,
        HMMPathGraph[] setOfPathGraphs)
    {
        HMMWithLogLikelihood hmm = createInitialHMM(setOfDataSequences, setOfPathGraphs);
        
        double previousLogLikelihood = hmm.logLikelihood;
        while (true) {
            hmm = trainNextHMM(setOfDataSequences, setOfPathGraphs, hmm.model);
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
        HMMPathGraph[] setOfPathGraphs,
        HiddenMarkovModel previousModel)
    {
        
        int states = previousModel.getNumOfNodes();
        int maxNumOfExitArcs = previousModel.getMaxNumOfExitArcs();
        OnlineMultivariateDataStatistics[] statistics =
                new OnlineMultivariateDataStatistics[states];
        double[][] transitionLikelihoods = new double[states][maxNumOfExitArcs];
        double[] statesLikelihoods = new double[states];
        
        for (int i = 0; i < setOfDataSequences.length; ++i) {
            HMMNodesProbabilities[] nodesProbs =
                previousModel.calculateStatesProbabilites(setOfDataSequences[i], setOfPathGraphs[i]);
            for (int it = 0; it < nodesProbs.length; ++it) {
                HMMNodesProbabilities probs = nodesProbs[it];
                for (int j = 0; j < states; ++j) {
                    double stateProb = probs.getStateProbability(j);
                    for (int k = 0; k < maxNumOfExitArcs; ++k) {
                        transitionLikelihoods[j][k] =
                            logAdd(transitionLikelihoods[j][k], probs.getArcProbability(j, k));
                    }
                    statesLikelihoods[j] = logAdd(statesLikelihoods[j], stateProb);
                    statistics[j].addPoint(setOfDataSequences[i][it], stateProb);
                }
            }
        }
        
        MultivariateNormalDistribution[] stateDistributions = new MultivariateNormalDistribution[states];
        for (int i = 0; i < states; ++i) {
            stateDistributions[i] = statistics[i].getDistribution();
            for (int j = 0; j < maxNumOfExitArcs; ++j)
                transitionLikelihoods[i][j] -= statesLikelihoods[i];
        }
        return createNextHMM(setOfPathGraphs, stateDistributions, transitionLikelihoods);
    }

    private double logAdd(double d, double arcProbability)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    private HMMWithLogLikelihood createNextHMM(
        HMMPathGraph[] setOfStateGraphs,
        MultivariateNormalDistribution[] stateDistributions,
        double[][] transitionProbabilities)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private HMMWithLogLikelihood createInitialHMM(double[][][] setOfSequences, HMMPathGraph[] setOfPathGraphs)
    {
        HMMNode[] nodes = createInitialNodes(setOfPathGraphs);
        // TODO Auto-generated method stub
        return new HMMWithLogLikelihood(null, 0);
    }
    
    private HMMNode[] createInitialNodes(HMMPathGraph[] setOfPathGraphs)
    {
        // TODO Auto-generated method stub
        return new HMMNode[0];
    }
}

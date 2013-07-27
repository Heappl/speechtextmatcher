package common.algorithms.hmm;

import common.algorithms.IObservationScorer;

public class HiddenMarkovModel
{
    private IObservationScorer[] statesScorers;
    private double[][] transitionLogLikelihoods;
    private double[] initialStatesLogLikelihoods;
    
    public HiddenMarkovModel(double[] initialStatesLogLikelihoods,
                             IObservationScorer[] statesScorers,
                             double[][] transitionLogLikelihoods)
    {
        this.statesScorers = statesScorers;
        this.transitionLogLikelihoods = transitionLogLikelihoods;
        this.initialStatesLogLikelihoods = initialStatesLogLikelihoods;
    }
    
    public double[][] calculateStatesProbabilitesGivenSequenceOfObservations(double[][] sequence)
    {
        double[][] ret = new double[sequence.length + 1][statesScorers.length];
        for (int s = 0; s < statesScorers.length; ++s) {
            ret[0][s] = this.initialStatesLogLikelihoods[s];
        }
        for (int t = 0; t < sequence.length; ++t) {
            double[] observation = sequence[t];
            for (int s = 0; s < statesScorers.length; ++s)
                ret[t + 1][s] = Double.NEGATIVE_INFINITY;
            for (int s = 0; s < statesScorers.length; ++s) {
                double observationScore = this.statesScorers[s].score(observation);
                for (int i = 0; i < statesScorers.length; ++i) {
                    double auxScore = observationScore + ret[t][i] + transitionLogLikelihoods[i][s];
                    ret[t + 1][s] = Math.max(ret[t + 1][s], auxScore);
                }
            }
        }
        return ret;
    }
}

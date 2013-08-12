package phonemeAligner.hmmBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.LogMath;
import common.exceptions.ImplementationError;

public class MultipleGaussianTrainer
{
    private class ScorerLogLikelihood
    {
        float logLikelihood;
        final GaussianObservationScorer scorer;
        
        public ScorerLogLikelihood(float logLikelihood, GaussianObservationScorer scorer)
        {
            this.logLikelihood = logLikelihood;
            this.scorer = scorer;
        }
    };
    
    private ArrayList<ScorerLogLikelihood> currentObservationLikelihoods =
            new ArrayList<ScorerLogLikelihood>();
    private double[] currentObservation = null;
    private boolean secondPhaseStarted = false;

    public void addObservation(double[] observation, float logLikelihood, GaussianObservationScorer singleGaussian)
    {
        if (currentObservation == null) currentObservation = observation;
        if (currentObservation != observation) flushObservation();
        currentObservation = observation;
        currentObservationLikelihoods.add(new ScorerLogLikelihood(logLikelihood, singleGaussian));
    }

    public void finishTraining()
    {
        flushObservationAgain();
        currentObservationLikelihoods.clear();
        secondPhaseStarted = false;
    }

    private void flushObservationAgain()
    {
//        normalize();
        for (ScorerLogLikelihood scorerWithLL : currentObservationLikelihoods) {
            scorerWithLL.scorer.addObservationAgain(currentObservation, scorerWithLL.logLikelihood);
        }
        currentObservationLikelihoods.clear();
    }

    public void addObservationAgain(double[] observation, float logLikelihood, GaussianObservationScorer singleGaussian)
    {
        if (!secondPhaseStarted) initiateSecondPhase();
        if (currentObservation == null) currentObservation = observation;
        if (currentObservation != observation) flushObservationAgain();
        currentObservation = observation;
        currentObservationLikelihoods.add(new ScorerLogLikelihood(logLikelihood, singleGaussian));
    }

    private void initiateSecondPhase()
    {
        flushObservation();
        currentObservationLikelihoods.clear();
        secondPhaseStarted = true;
        currentObservation = null;
    }

    private void flushObservation()
    {
//        normalize();
        for (ScorerLogLikelihood scorerWithLL : currentObservationLikelihoods) {
            scorerWithLL.scorer.addObservation(currentObservation, scorerWithLL.logLikelihood);
        }
        currentObservationLikelihoods.clear();
    }

    private void normalize()
    {
        LogMath sum = new LogMath();
        for (ScorerLogLikelihood scorerWithLL : currentObservationLikelihoods) {
            sum.logAdd(scorerWithLL.logLikelihood);
        }
        for (ScorerLogLikelihood scorerWithLL : currentObservationLikelihoods) {
            scorerWithLL.logLikelihood -= sum.getResult();
        }
    }
}

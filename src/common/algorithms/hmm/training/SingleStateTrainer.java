package common.algorithms.hmm.training;

import common.algorithms.hmm.State;

public class SingleStateTrainer
{
    private final State state;

    public SingleStateTrainer(State state)
    {
        this.state = state;
    }

    public State getState()
    {
        return this.state;
    }
    
    public void finish()
    {
        this.state.getTrainableScorer().finishTraining();
    }

    public void addObservation(double[] observation, float logLikelihood)
    {
        this.state.getTrainableScorer().addObservation(observation, logLikelihood);
    }

    public void addObservationAgain(double[] observation, float logLikelihood)
    {
        this.state.getTrainableScorer().addObservationAgain(observation, logLikelihood);
    }
}

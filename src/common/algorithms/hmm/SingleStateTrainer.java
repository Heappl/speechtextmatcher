package common.algorithms.hmm;

public class SingleStateTrainer
{
    private final State state;

    public SingleStateTrainer(State state)
    {
        this.state = state;
        this.state.getTrainableScorer().startTraining();
    }

    public State getState()
    {
        return this.state;
    }
    
    public void finish()
    {
        this.state.getTrainableScorer().finishTraining();
    }

    public void addObservation(double[] observation, double logLikelihood)
    {
        this.state.getTrainableScorer().addObservation(observation, logLikelihood);
    }
}

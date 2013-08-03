package common.algorithms.hmm;

public class TransitionTrainer
{
    private StateExit exit;
    private double totalExitLikelihood = 0;
    private double totalStateLikelihood = 0;

    public TransitionTrainer(StateExit exit)
    {
        this.exit = exit;
    }
    public StateExit getTransition()
    {
        return this.exit;
    }
    public void addObservation(double[] observation, double likelihood)
    {
        this.totalExitLikelihood = LogMath.logAdd(this.totalExitLikelihood, likelihood);
    }
    public void addStateObservation(double[] observation, double likelihood)
    {
        this.totalStateLikelihood = LogMath.logAdd(this.totalStateLikelihood, likelihood);
    }
    public void finish()
    {
        this.exit.updateLikelihood(this.totalExitLikelihood - this.totalStateLikelihood);
    }
}

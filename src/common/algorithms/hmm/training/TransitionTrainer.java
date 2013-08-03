package common.algorithms.hmm.training;

import common.algorithms.hmm.LogMath;
import common.algorithms.hmm.StateExit;

public class TransitionTrainer
{
    private StateExit exit;
    private float totalExitLikelihood = 0;
    private float totalStateLikelihood = 0;

    public TransitionTrainer(StateExit exit)
    {
        this.exit = exit;
    }
    public StateExit getTransition()
    {
        return this.exit;
    }
    public void addObservation(double[] observation, float likelihood)
    {
        this.totalExitLikelihood = LogMath.logAdd(this.totalExitLikelihood, likelihood);
    }
    public void addStateObservation(double[] observation, float likelihood)
    {
        this.totalStateLikelihood = LogMath.logAdd(this.totalStateLikelihood, likelihood);
    }
    public void finish()
    {
        this.exit.updateLikelihood(this.totalExitLikelihood - this.totalStateLikelihood);
    }
}

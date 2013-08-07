package common.algorithms.hmm.training;

import common.algorithms.hmm.LogMath;
import common.algorithms.hmm.StateExit;

public class TransitionTrainer
{
    private StateExit exit;
    private float totalExitLikelihood = Float.NEGATIVE_INFINITY;
    private float totalStateLikelihood = Float.POSITIVE_INFINITY;

    public TransitionTrainer(StateExit exit)
    {
        this.exit = exit;
    }
    public StateExit getTransition()
    {
        return this.exit;
    }
    public void addObservation(float likelihood)
    {
        if (likelihood == Float.NEGATIVE_INFINITY) return;
        if (this.totalExitLikelihood == Float.NEGATIVE_INFINITY)
            this.totalExitLikelihood = likelihood;
        else
            this.totalExitLikelihood = LogMath.logAdd(this.totalExitLikelihood, likelihood);
    }
    public void addStateObservation(float likelihood)
    {
        if (likelihood == Float.NEGATIVE_INFINITY) return;
        if (this.totalStateLikelihood == Float.POSITIVE_INFINITY)
            this.totalStateLikelihood = likelihood;
        else
            this.totalStateLikelihood = LogMath.logAdd(this.totalStateLikelihood, likelihood);
    }
    public void finish()
    {
        this.exit.updateLikelihood(this.totalExitLikelihood - this.totalStateLikelihood);
    }
}

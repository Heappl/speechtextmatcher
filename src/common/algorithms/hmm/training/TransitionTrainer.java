package common.algorithms.hmm.training;

import common.LogMath;
import common.algorithms.hmm.StateExit;

public class TransitionTrainer
{
    private StateExit exit;
    private LogMath totalExitLikelihood = new LogMath();
    private LogMath totalStateLikelihood = new LogMath();

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
        this.totalExitLikelihood.logAdd(likelihood);
    }
    public void addStateObservation(float likelihood)
    {
        if (likelihood == Float.NEGATIVE_INFINITY) return;
        this.totalStateLikelihood.logAdd(likelihood);
    }
    public void finish()
    {
        this.exit.updateLikelihood(
            this.totalExitLikelihood.getResult() - this.totalStateLikelihood.getResult());
    }
}

package common.algorithms.hmm.training;

import common.LogMath;
import common.algorithms.hmm.StateExit;
import common.exceptions.ImplementationError;

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
    public void finish() throws ImplementationError
    {
        if (this.totalExitLikelihood.getResult() > this.totalStateLikelihood.getResult())
            throw new ImplementationError("total exit likelihood is greater than total state likelihood: "
                    + this.totalExitLikelihood.getResult() + " > " + this.totalStateLikelihood.getResult());
        this.exit.updateLikelihood(
            this.totalExitLikelihood.getResult() - this.totalStateLikelihood.getResult());
    }
}

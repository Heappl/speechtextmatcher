package common.algorithms.hmm;

import java.util.ArrayList;
import java.util.Iterator;

public class State implements Iterable<StateExit>
{
    private final ArrayList<StateExit> exits;
    private final ITrainableObservationLogLikelihoodCalculator trainableScorer;
    
    public State(
        ArrayList<StateExit> exits,
        ITrainableObservationLogLikelihoodCalculator trainableScorer)
    {
        this.exits = exits;
        this.trainableScorer = trainableScorer;
    }

    @Override
    public Iterator<StateExit> iterator()
    {
        return this.exits.iterator();
    }

    public ITrainableObservationLogLikelihoodCalculator getTrainableScorer()
    {
        return this.trainableScorer;
    }

    public float observationLogLikelihood(double[] observation)
    {
        return this.trainableScorer.observationLogLikelihood(observation);
    }
}


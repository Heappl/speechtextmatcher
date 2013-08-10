package common.algorithms.hmm;

import java.util.ArrayList;
import java.util.Iterator;

import common.exceptions.ImplementationError;

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
    
    public int numOfExits()
    {
        return this.exits.size();
    }

    public ITrainableObservationLogLikelihoodCalculator getTrainableScorer()
    {
        return this.trainableScorer;
    }

    public float observationLogLikelihood(double[] observation) throws ImplementationError
    {
        float ret = this.trainableScorer.observationLogLikelihood(observation);
//        if (ret > 0) throw new ImplementationError("observation likelihood is greater than 0: " + ret);
        return ret;
    }
}


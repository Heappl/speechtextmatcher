package common.algorithms.hmm;

import common.exceptions.ImplementationError;

public interface IObservationScorer
{
    public double score(double[] observation) throws ImplementationError;
    public String getName();
}

package common.algorithms.hmm;

import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.exceptions.ImplementationError;

public class GaussianScorer implements IObservationScorer
{
    private String name;
    private MultivariateNormalDistribution distribution;
    
    public GaussianScorer(String name, MultivariateNormalDistribution distribution)
    {
        this.name = name;
        this.distribution = distribution;
    }
    
    @Override
    public double score(double[] observation) throws ImplementationError
    {
        return this.distribution.logLikelihood(observation);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

}

package algorithms;

import commonExceptions.ImplementationError;

public class MixtureGaussianModel
{
    private MultivariateNormalDistribution[] mixtures;
    
    public MixtureGaussianModel(MultivariateNormalDistribution[] mixtures)
    {
        this.mixtures = mixtures;
    }
    
    public double logLikelihood(double[] point) throws ImplementationError
    {
        double ret = 0;
        for (MultivariateNormalDistribution mixture : mixtures) {
            ret += Math.pow(Math.E, mixture.logLikelihood(point));
        }
        return Math.log(ret);
    }
}

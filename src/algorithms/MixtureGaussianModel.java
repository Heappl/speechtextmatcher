package algorithms;

import commonExceptions.ImplementationError;

public class MixtureGaussianModel
{
    private MultivariateNormalDistribution[] mixtures;
    private final static double maxLog = Math.log(Double.MAX_VALUE);
    
    public MixtureGaussianModel(MultivariateNormalDistribution[] mixtures)
    {
        this.mixtures = mixtures;
    }
    
    public double logLikelihood(double[] point) throws ImplementationError
    {
        double ret = 0;
        boolean ok = false;
        for (MultivariateNormalDistribution mixture : mixtures) {
            double mixtureResult = mixture.logLikelihood(point);
            if (mixtureResult == Double.NaN) continue;
            ok = true;
            ret += Math.pow(Math.E, -mixtureResult);
        }
        if (!ok) return Double.NEGATIVE_INFINITY;
        if (ret == Double.POSITIVE_INFINITY) ret = Double.MAX_VALUE; 
        return -Math.log(ret);
    }
    
    public String toString()
    {
        String ret = "{";
        for (MultivariateNormalDistribution mixture : mixtures) {
            ret += mixture + ", ";
        }
        return ret.substring(0, ret.length() - 2) + "}";
    }
}

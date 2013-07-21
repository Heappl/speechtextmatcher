package common.algorithms;

import common.exceptions.DeserializationException;
import common.exceptions.ImplementationError;

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

    public String serialize()
    {
        String ret = "{";
        for (MultivariateNormalDistribution mixture : mixtures) {
            ret += mixture.serialize() + ",";
        }
        return ret.substring(0, ret.length() - 1) + "}";
    }

    public static MixtureGaussianModel deserialize(String modelData) throws DeserializationException
    {
        String[] mixturesData = modelData.substring(1, modelData.length() - 1).split(",");
        MultivariateNormalDistribution[] mixtures = new MultivariateNormalDistribution[mixturesData.length];
        for (int i = 0; i < mixturesData.length; ++i)
            mixtures[i] = MultivariateNormalDistribution.deserialize(mixturesData[i]);
        return new MixtureGaussianModel(mixtures);
    }
}

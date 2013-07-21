package phonemeScorers;


import common.algorithms.MultivariateNormalDistribution;
import common.exceptions.DeserializationException;
import common.exceptions.ImplementationError;

public class SingleGaussianPhonemeScorer implements IPhonemeScorer
{

    private MultivariateNormalDistribution model;
    private String phoneme;
    
    public SingleGaussianPhonemeScorer(MultivariateNormalDistribution gaussianModel, String phoneme)
    {
        this.model = gaussianModel;
        this.phoneme = phoneme;
    }
    
    public String getPhoneme() { return phoneme; }
    
    public double score(double[] data) throws ImplementationError
    {
        return model.logLikelihood(data);
    }
    
    public String toString()
    {
        return phoneme + " " + model; 
    }

    @Override
    public String serialize()
    {
        return this.getClass().getCanonicalName() + "{" + phoneme + ":" + model.serialize() + "}";
    }

    @Override
    public IPhonemeScorer deserialize(String line) throws DeserializationException
    {
        int prefixLength = this.getClass().getCanonicalName().length() + 1;
        String phoneme = line.substring(prefixLength, line.length() - 1).split(":")[0];
        String modelData = line.substring(prefixLength + phoneme.length() + 1, line.length() - 1);
        MultivariateNormalDistribution model = MultivariateNormalDistribution.deserialize(modelData);
        return new SingleGaussianPhonemeScorer(model, phoneme);
    }
}

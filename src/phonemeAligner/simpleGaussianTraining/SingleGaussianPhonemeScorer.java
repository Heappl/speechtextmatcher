package phonemeAligner.simpleGaussianTraining;

import phonemeAligner.IPhonemeScorer;
import algorithms.MultivariateNormalDistribution;

import commonExceptions.ImplementationError;

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
}

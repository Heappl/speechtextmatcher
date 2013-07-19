package phonemeAligner.simpleGaussianTraining;

import phonemeAligner.IPhonemeScorer;
import commonExceptions.ImplementationError;

import edu.cmu.sphinx.frontend.FloatData;
import algorithms.MixtureGaussianModel;

public class GaussianMixturePhonemeScorer implements IPhonemeScorer
{
    private MixtureGaussianModel gaussianMixtureModel;
    private String phoneme;
    
    public GaussianMixturePhonemeScorer(MixtureGaussianModel gaussianMixtureModel, String phoneme)
    {
        this.gaussianMixtureModel = gaussianMixtureModel;
        this.phoneme = phoneme;
    }
    
    public String getPhoneme() { return phoneme; }
    
    public double score(double[] data) throws ImplementationError
    {
        return gaussianMixtureModel.logLikelihood(data);
    }
    
    public String toString()
    {
        return phoneme + " " + gaussianMixtureModel; 
    }
}

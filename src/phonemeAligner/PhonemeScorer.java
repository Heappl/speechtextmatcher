package phonemeAligner;

import commonExceptions.ImplementationError;

import edu.cmu.sphinx.frontend.FloatData;
import algorithms.MixtureGaussianModel;

public class PhonemeScorer
{
    private MixtureGaussianModel gaussianMixtureModel;
    private String phoneme;
    
    public PhonemeScorer(MixtureGaussianModel gaussianMixtureModel, String phoneme)
    {
        this.gaussianMixtureModel = gaussianMixtureModel;
        this.phoneme = phoneme;
    }
    
    public String getPhoneme() { return phoneme; }
    
    public double score(FloatData data, double power) throws ImplementationError
    {
        double[] point = new double[data.getValues().length + 1];
        point[0] = power;
        for (int i = 1; i < point.length; ++i)
            point[i] = data.getValues()[i - 1];
        return gaussianMixtureModel.logLikelihood(point);
    }
}

package phonemeScorers;

import common.algorithms.MixtureGaussianModel;
import common.exceptions.DeserializationException;
import common.exceptions.ImplementationError;

import edu.cmu.sphinx.frontend.FloatData;

public class GaussianMixturePhonemeScorer implements IPhonemeScorer
{
    private MixtureGaussianModel gaussianMixtureModel = null;
    private String phoneme = "";

    public GaussianMixturePhonemeScorer()
    {
    }
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

    @Override
    public String serialize()
    {
        return this.getClass().getCanonicalName() + "{" + phoneme + ":" + gaussianMixtureModel.serialize() + "}";
    }

    @Override
    public IPhonemeScorer deserialize(String line) throws DeserializationException
    {
        int prefixLength = this.getClass().getCanonicalName().length() + 1;
        String phoneme = line.substring(prefixLength, line.length() - 1).split(":")[0];
        String modelData = line.substring(prefixLength + phoneme.length() + 1, line.length() - 1);
        MixtureGaussianModel model = MixtureGaussianModel.deserialize(modelData);
        return new GaussianMixturePhonemeScorer(model, phoneme);
    }
}

package phonemeScorers;

import common.LogMath;
import common.algorithms.gaussian.MixtureGaussianModel;
import common.exceptions.DeserializationException;
import common.exceptions.ImplementationError;

public class GaussianMixturePhonemeScorer implements IPhonemeScorer
{
    private MixtureGaussianModel gaussianMixtureModel = null;
    private String phoneme = "";
    private float transitionScore = 0;

    public GaussianMixturePhonemeScorer()
    {
    }
    public GaussianMixturePhonemeScorer(
        MixtureGaussianModel gaussianMixtureModel,
        float transitionScore,
        String phoneme)
    {
        this.gaussianMixtureModel = gaussianMixtureModel;
        this.phoneme = phoneme;
        this.transitionScore = transitionScore;
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
        return
                this.getClass().getCanonicalName()
                + "{"
                + phoneme + ":" + this.transitionScore + ":"
                + gaussianMixtureModel.serialize()
                + "}";
    }

    @Override
    public IPhonemeScorer deserialize(String line) throws DeserializationException
    {
        int prefixLength = this.getClass().getCanonicalName().length() + 1;
        String phoneme = line.substring(prefixLength, line.length() - 1).split(":")[0];
        String transitionScore = line.substring(prefixLength, line.length() - 1).split(":")[1];
        
        String modelData = line.substring(
                prefixLength + phoneme.length() + transitionScore.length() + 2,
                line.length() - 1);
        MixtureGaussianModel model = MixtureGaussianModel.deserialize(modelData);
        return new GaussianMixturePhonemeScorer(model, Float.valueOf(transitionScore), phoneme);
    }
    @Override
    public float transitionScore()
    {
        return this.transitionScore;
    }
    @Override
    public float noTransitionScore()
    {
        return LogMath.linearToLog(1 - LogMath.logToLinear(this.transitionScore));
    }
}

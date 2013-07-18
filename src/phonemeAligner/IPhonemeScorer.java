package phonemeAligner;

import commonExceptions.ImplementationError;

import edu.cmu.sphinx.frontend.FloatData;

public interface IPhonemeScorer
{
    public String getPhoneme();
    public double score(double[] data) throws ImplementationError;
}

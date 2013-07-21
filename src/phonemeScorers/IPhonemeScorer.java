package phonemeScorers;

import common.exceptions.DeserializationException;
import common.exceptions.ImplementationError;

public interface IPhonemeScorer
{
    public String getPhoneme();
    public double score(double[] data) throws ImplementationError;
    public String serialize();
    public IPhonemeScorer deserialize(String line) throws DeserializationException;
}

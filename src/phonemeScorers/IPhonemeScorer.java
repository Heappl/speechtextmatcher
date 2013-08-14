package phonemeScorers;

import common.exceptions.DeserializationException;

public interface IPhonemeScorer
{
    public String getPhoneme();
    public double score(double[] data);
    public double transitionScore();
    public String serialize();
    public IPhonemeScorer deserialize(String line) throws DeserializationException;
}

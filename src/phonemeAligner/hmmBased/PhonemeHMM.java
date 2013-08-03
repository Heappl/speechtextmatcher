package phonemeAligner.hmmBased;

import common.algorithms.hmm.BestSequenceFinder;

public class PhonemeHMM
{
    private final HMMGraphFromPhonemeSequenceCreator hmmGraphCreator;
    
    public PhonemeHMM(HMMGraphFromPhonemeSequenceCreator hmmGraphCreator)
    {
        this.hmmGraphCreator = hmmGraphCreator;
    }
    public String[] calculateMostProbableSequence(double[][] audioData, String text)
    {
        return new BestSequenceFinder().findBestSequence(audioData, this.hmmGraphCreator.create(text));
    }
}
